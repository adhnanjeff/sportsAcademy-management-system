package com.badminton.academy.service;

import com.badminton.academy.model.ActivityLog;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.ActivityAction;
import com.badminton.academy.model.enums.EntityType;
import com.badminton.academy.repository.ActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing activity logs and audit trails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    /**
     * Log an activity
     */
    @Transactional
    public ActivityLog logActivity(
        ActivityAction action,
        EntityType entityType,
        Long entityId,
        String details,
        User user
    ) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .user(user)
                .userName(user != null ? user.getUsername() : "System")
                .ipAddress(getClientIP())
                .userAgent(getUserAgent())
                .build();

            return activityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.error("Failed to log activity: {} for entity {}/{}", action, entityType, entityId, e);
            // Don't throw exception - logging failures shouldn't break the main operation
            return null;
        }
    }

    /**
     * Log an activity without user context (system actions)
     */
    @Transactional
    public ActivityLog logSystemActivity(
        ActivityAction action,
        EntityType entityType,
        Long entityId,
        String details
    ) {
        return logActivity(action, entityType, entityId, details, null);
    }

    /**
     * Get activity logs for a specific entity
     */
    public List<ActivityLog> getEntityActivityLogs(EntityType entityType, Long entityId) {
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Get activity logs by entity type with pagination
     */
    public Page<ActivityLog> getActivityLogsByType(EntityType entityType, int page, int size) {
        return activityLogRepository.findByEntityTypeOrderByTimestampDesc(
            entityType,
            PageRequest.of(page, size)
        );
    }

    /**
     * Get activity logs for a user
     */
    public Page<ActivityLog> getUserActivityLogs(Long userId, int page, int size) {
        return activityLogRepository.findByUserIdOrderByTimestampDesc(
            userId,
            PageRequest.of(page, size)
        );
    }

    /**
     * Get recent activity logs
     */
    public List<ActivityLog> getRecentActivities(int limit) {
        return activityLogRepository.findTop100ByOrderByTimestampDesc()
            .stream()
            .limit(limit)
            .toList();
    }

    /**
     * Get activity logs within a date range
     */
    public List<ActivityLog> getActivityLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return activityLogRepository.findByTimestampBetween(startDate, endDate);
    }

    /**
     * Clean up old activity logs (e.g., older than 90 days)
     */
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        activityLogRepository.deleteByTimestampBefore(cutoffDate);
        log.info("Cleaned up activity logs older than {} days", daysToKeep);
    }

    /**
     * Get client IP address from request
     */
    private String getClientIP() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Failed to get user agent: {}", e.getMessage());
        }
        return null;
    }
}
