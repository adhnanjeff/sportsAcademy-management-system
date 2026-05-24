package com.badminton.academy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.net.URI;
import java.net.URISyntaxException;

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
     * 
     * @return Configured HikariDataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }
        
        log.info("Configuring database connection...");
        
        HikariConfig config = new HikariConfig();
        
        try {
            // Parse the DATABASE_URL (format: postgres://user:password@host:port/database)
            URI dbUri = new URI(databaseUrl);
            
            String username = null;
            String password = null;
            
            // Extract username and password from userInfo
            if (dbUri.getUserInfo() != null) {
                String[] userInfo = dbUri.getUserInfo().split(":", 2);
                username = userInfo[0];
                if (userInfo.length > 1) {
                    password = userInfo[1];
                }
            }
            
            // Build JDBC URL with SSL for Render
            String host = dbUri.getHost();
            int port = dbUri.getPort() > 0 ? dbUri.getPort() : 5432;
            String database = dbUri.getPath().substring(1); // Remove leading slash
            
            // Build JDBC URL with proper SSL settings for Render PostgreSQL
            String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%d/%s?sslmode=require",
                host, port, database
            );
            
            log.info("Database host: {}, port: {}, database: {}", host, port, database);
            
            config.setJdbcUrl(jdbcUrl);
            
            if (username != null) {
                config.setUsername(username);
            }
            if (password != null) {
                config.setPassword(password);
            }
            
        } catch (URISyntaxException e) {
            log.error("Failed to parse DATABASE_URL", e);
            throw new IllegalStateException("Invalid DATABASE_URL format: " + e.getMessage());
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
        
        // Additional properties for Render PostgreSQL compatibility
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        config.addDataSourceProperty("ApplicationName", "badminton-academy");
        
        return new HikariDataSource(config);
    }
}
