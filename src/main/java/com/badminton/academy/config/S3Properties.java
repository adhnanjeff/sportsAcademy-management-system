package com.badminton.academy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3Properties {
    private String bucketName;
    private String region;
    private String accessKey;
    private String secretKey;
    private long maxFileSize = 10485760; // 10MB default
    private String[] allowedFileTypes = {"image/jpeg", "image/png", "image/jpg", "image/gif", "image/webp"};
}
