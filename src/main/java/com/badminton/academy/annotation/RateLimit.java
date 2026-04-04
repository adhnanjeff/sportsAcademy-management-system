package com.badminton.academy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for rate limiting API endpoints
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Maximum number of requests allowed
     */
    int maxRequests() default 10;
    
    /**
     * Time window in seconds
     */
    int windowSeconds() default 60;
    
    /**
     * Custom key for rate limiting (optional)
     * If not provided, uses user ID + method name
     */
    String key() default "";
}
