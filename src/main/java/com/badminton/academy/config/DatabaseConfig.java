package com.badminton.academy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database configuration for production environment.
 * Handles conversion of Render's postgres:// URL format to JDBC format.
 */
@Configuration
@Profile("prod")
@Slf4j
public class DatabaseConfig {

    /**
     * Creates a DataSource bean for production environment.
     * Properly parses Render's postgres:// URL to extract username, password, and host.
     * Supports multiple URL formats including internal Render URLs.
     * 
     * @return Configured HikariDataSource
     */
    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }
        
        log.info("Configuring database connection...");
        log.info("DATABASE_URL format detected: {}", databaseUrl.substring(0, Math.min(30, databaseUrl.length())) + "...");
        
        HikariConfig config = new HikariConfig();
        
        // Pattern to parse postgres://user:password@host:port/database or variations
        // Also handles URLs without database path (uses query params instead)
        Pattern pattern = Pattern.compile(
            "postgres(?:ql)?://([^:]+):([^@]+)@([^:/]+)(?::(\\d+))?(?:/([^?]+))?(?:\\?(.+))?"
        );
        
        Matcher matcher = pattern.matcher(databaseUrl);
        
        if (matcher.matches()) {
            String username = matcher.group(1);
            String password = matcher.group(2);
            String host = matcher.group(3);
            String portStr = matcher.group(4);
            String database = matcher.group(5);
            String queryParams = matcher.group(6);
            
            int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : 5432;
            
            // If database is null/empty, check for 'dbname' in query params
            if (database == null || database.isEmpty()) {
                if (queryParams != null) {
                    Pattern dbPattern = Pattern.compile("dbname=([^&]+)");
                    Matcher dbMatcher = dbPattern.matcher(queryParams);
                    if (dbMatcher.find()) {
                        database = dbMatcher.group(1);
                    }
                }
                // Default database name if not found
                if (database == null || database.isEmpty()) {
                    database = username; // PostgreSQL default: database same as username
                }
            }
            
            // Build JDBC URL with proper SSL settings for Render/Supabase PostgreSQL
            StringBuilder jdbcUrl = new StringBuilder();
            jdbcUrl.append("jdbc:postgresql://").append(host).append(":").append(port).append("/").append(database);
            jdbcUrl.append("?sslmode=require");
            // CRITICAL: Disable prepared statement caching to fix "prepared statement already exists" error with Supabase pooler
            jdbcUrl.append("&prepareThreshold=0");
            
            // Append any additional query parameters (but skip sslmode/prepareThreshold if already present)
            if (queryParams != null && !queryParams.isEmpty()) {
                for (String param : queryParams.split("&")) {
                    if (!param.startsWith("sslmode=") && !param.startsWith("dbname=") && !param.startsWith("prepareThreshold=")) {
                        jdbcUrl.append("&").append(param);
                    }
                }
            }
            
            log.info("Database host: {}, port: {}, database: {}", host, port, database);
            
            config.setJdbcUrl(jdbcUrl.toString());
            config.setUsername(username);
            config.setPassword(password);
            
        } else {
            // Fallback: try simple replacement for JDBC format
            log.warn("Could not parse DATABASE_URL with regex, attempting simple conversion");
            String jdbcUrl = databaseUrl;
            if (jdbcUrl.startsWith("postgres://")) {
                jdbcUrl = jdbcUrl.replace("postgres://", "jdbc:postgresql://");
            } else if (jdbcUrl.startsWith("postgresql://")) {
                jdbcUrl = jdbcUrl.replace("postgresql://", "jdbc:postgresql://");
            }

            // Add SSL if not present
            if (!jdbcUrl.contains("sslmode=")) {
                jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
            }
            // Add prepareThreshold=0 to fix Supabase pooler issues
            if (!jdbcUrl.contains("prepareThreshold=")) {
                jdbcUrl += "&prepareThreshold=0";
            }

            config.setJdbcUrl(jdbcUrl);

            String envUsername = System.getenv("DATABASE_USERNAME");
            String envPassword = System.getenv("DATABASE_PASSWORD");
            if (envUsername != null && !envUsername.isEmpty()) {
                config.setUsername(envUsername);
            }
            if (envPassword != null && !envPassword.isEmpty()) {
                config.setPassword(envPassword);
            }

            log.info("Using JDBC URL: {}", jdbcUrl.substring(0, Math.min(50, jdbcUrl.length())) + "...");
        }
        
        // Connection pool settings optimized for Render free tier
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(60000); // 60 seconds for Render's slow startup
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setLeakDetectionThreshold(60000);
        
        // Additional properties for Render/Supabase PostgreSQL compatibility
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        config.addDataSourceProperty("ApplicationName", "badminton-academy");
        // Disable prepared statement caching to fix pooler issues
        config.addDataSourceProperty("prepareThreshold", "0");
        
        return new HikariDataSource(config);
    }
}
