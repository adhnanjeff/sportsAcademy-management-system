package com.badminton.academy.aspect;

import com.badminton.academy.annotation.RateLimit;
import com.badminton.academy.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Aspect for implementing rate limiting on API endpoints
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final Map<String, CopyOnWriteArrayList<Long>> requestCounts = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String rateLimitKey = getRateLimitKey(joinPoint, rateLimit);
        
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (rateLimit.windowSeconds() * 1000L);

        // Get or create request list for this key
        CopyOnWriteArrayList<Long> timestamps = requestCounts.computeIfAbsent(
            rateLimitKey, 
            k -> new CopyOnWriteArrayList<>()
        );

        // Remove old timestamps outside the window
        timestamps.removeIf(timestamp -> timestamp < windowStart);

        // Check if rate limit is exceeded
        if (timestamps.size() >= rateLimit.maxRequests()) {
            long oldestTimestamp = timestamps.isEmpty() ? currentTime : timestamps.get(0);
            long retryAfter = (windowStart - oldestTimestamp + (rateLimit.windowSeconds() * 1000L)) / 1000;
            
            log.warn("Rate limit exceeded for key: {}. Max: {}, Window: {}s", 
                rateLimitKey, rateLimit.maxRequests(), rateLimit.windowSeconds());
            
            throw new TooManyRequestsException(
                String.format("Rate limit exceeded. Please try again in %d seconds.", retryAfter)
            );
        }

        // Add current request timestamp
        timestamps.add(currentTime);

        // Clean up old entries periodically
        cleanupOldEntries();

        return joinPoint.proceed();
    }

    private String getRateLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        if (!rateLimit.key().isEmpty()) {
            return rateLimit.key();
        }

        // Get user identifier from request
        String userIdentifier = getUserIdentifier();
        
        // Get method name
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        
        return userIdentifier + ":" + methodName;
    }

    private String getUserIdentifier() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Try to get user from security context or session
                Object principal = request.getUserPrincipal();
                if (principal != null) {
                    return principal.toString();
                }
                
                // Fallback to IP address
                return getClientIP(request);
            }
        } catch (Exception e) {
            log.warn("Failed to get user identifier: {}", e.getMessage());
        }
        return "anonymous";
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Periodically clean up old entries to prevent memory leaks
     */
    private void cleanupOldEntries() {
        if (requestCounts.size() > 10000) {
            log.info("Cleaning up rate limit cache, current size: {}", requestCounts.size());
            long cutoffTime = System.currentTimeMillis() - (3600 * 1000); // 1 hour ago
            requestCounts.entrySet().removeIf(entry -> {
                entry.getValue().removeIf(timestamp -> timestamp < cutoffTime);
                return entry.getValue().isEmpty();
            });
        }
    }
}
