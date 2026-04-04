package com.badminton.academy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database configuration for production environment.
 * Handles conversion of Render's postgres:// URL format to JDBC format.
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    /**
     * Creates a DataSource bean for production environment.
     * Converts Render's postgres:// URL to jdbc:postgresql:// format.
     * 
     * @return Configured HikariDataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }
        
        // Convert postgres:// to jdbc:postgresql://
        if (databaseUrl.startsWith("postgres://")) {
            databaseUrl = databaseUrl.replace("postgres://", "jdbc:postgresql://");
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        
        // Connection pool settings optimized for Render free tier
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(60000); // 60 seconds for Render's slow startup
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setLeakDetectionThreshold(60000);
        
        return new HikariDataSource(config);
    }
}
