package com.badminton.academy.service;

import com.badminton.academy.config.S3Properties;
import com.badminton.academy.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    /**
     * Upload file to S3
     * @param file The file to upload
     * @param folder The folder/prefix to upload to (e.g., "students", "achievements")
     * @return The S3 URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) {
        String key = uploadFileToS3(file, folder);
        return getFileUrl(key);
    }

    /**
     * Upload file to S3 and return only the object key for durable storage
     */
    public String uploadFileAndReturnKey(MultipartFile file, String folder) {
        return uploadFileToS3(file, folder);
    }

    private String uploadFileToS3(MultipartFile file, String folder) {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String key = folder + "/" + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully uploaded file to S3: {}", key);
            return key;
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
    }

    /**
     * Delete file from S3
     * @param fileUrl The full S3 URL of the file
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String key = extractKey(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from S3: {}", key);
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Get presigned URL for temporary access to a file
     * @param key The S3 key of the file
     * @param duration Duration for which the URL should be valid
     * @return Presigned URL
     */
    public String getPresignedUrl(String key, Duration duration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Replace existing file with a new one
     * @param oldFileUrl URL of the old file to delete
     * @param newFile New file to upload
     * @param folder Folder to upload the new file to
     * @return URL of the newly uploaded file
     */
    public String replaceFile(String oldFileUrl, MultipartFile newFile, String folder) {
        // Delete old file if it exists
        if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
            try {
                deleteFile(oldFileUrl);
            } catch (Exception e) {
                log.warn("Failed to delete old file, continuing with upload: {}", e.getMessage());
            }
        }
        
        // Upload new file and return the key for durable storage
        return uploadFileAndReturnKey(newFile, folder);
    }

    /**
     * Validate file before upload
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or null");
        }

        if (file.getSize() > s3Properties.getMaxFileSize()) {
            throw new BadRequestException(
                    String.format("File size exceeds maximum allowed size of %d bytes", 
                            s3Properties.getMaxFileSize())
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(s3Properties.getAllowedFileTypes()).contains(contentType)) {
            throw new BadRequestException(
                    String.format("File type '%s' is not allowed. Allowed types: %s", 
                            contentType, 
                            String.join(", ", s3Properties.getAllowedFileTypes()))
            );
        }
    }

    /**
     * Generate unique file name
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Get the full S3 URL for a key
     * For private buckets, this returns a presigned URL valid for 7 days
     */
    private String getFileUrl(String key) {
        // For private buckets, return presigned URL valid for 7 days
        return getPresignedUrl(key, Duration.ofDays(7));
    }

    /**
     * Convert a stored key or URL into a fresh presigned URL for browser display.
     */
    public String resolveFileUrl(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return null;
        }
        String value = storedValue.trim();
        if ((value.startsWith("http://") || value.startsWith("https://")) && !value.contains("amazonaws.com")) {
            return value;
        }
        String key = extractKey(storedValue);
        return getFileUrl(key);
    }

    /**
     * Extract S3 key from either a raw key or full S3 URL.
     */
    public String extractKey(String fileUrlOrKey) {
        if (fileUrlOrKey == null || fileUrlOrKey.isBlank()) {
            throw new IllegalArgumentException("File URL or key cannot be empty");
        }

        String value = fileUrlOrKey.trim();

        // Already a key (common format: folder/filename.ext)
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return value;
        }

        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            if (host == null || !host.contains("amazonaws.com")) {
                throw new IllegalArgumentException("URL is not an S3 URL: " + value);
            }
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("Invalid S3 URL format: " + value);
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid S3 URL format: " + value, ex);
        }
    }

    /**
     * Check if file exists in S3
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking if file exists: {}", e.getMessage(), e);
            throw new RuntimeException("Error checking if file exists: " + e.awsErrorDetails().errorMessage());
        }
    }
}
