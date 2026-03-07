package com.badminton.academy.controller;

import com.badminton.academy.dto.response.FileUploadResponse;
import com.badminton.academy.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload", description = "Endpoints for managing file uploads to S3")
public class FileUploadController {

    private final S3Service s3Service;

    @PostMapping(value = "/upload/student-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Upload student profile image", 
               description = "Upload a profile image for a student. Allowed formats: JPEG, PNG, GIF, WebP. Max size: 10MB")
    public ResponseEntity<FileUploadResponse> uploadStudentProfile(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading student profile image: {}", file.getOriginalFilename());
        String fileUrl = s3Service.uploadFile(file, "students/profiles");
        
        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/achievement-certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Upload achievement certificate image", 
               description = "Upload a certificate or medal image for an achievement. Allowed formats: JPEG, PNG, GIF, WebP. Max size: 10MB")
    public ResponseEntity<FileUploadResponse> uploadAchievementCertificate(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading achievement certificate: {}", file.getOriginalFilename());
        String fileUrl = s3Service.uploadFile(file, "achievements/certificates");
        
        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/coach-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Upload coach profile image", 
               description = "Upload a profile image for a coach. Allowed formats: JPEG, PNG, GIF, WebP. Max size: 10MB")
    public ResponseEntity<FileUploadResponse> uploadCoachProfile(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading coach profile image: {}", file.getOriginalFilename());
        String fileUrl = s3Service.uploadFile(file, "coaches/profiles");
        
        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/parent-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    @Operation(summary = "Upload parent profile image", 
               description = "Upload a profile image for a parent. Allowed formats: JPEG, PNG, GIF, WebP. Max size: 10MB")
    public ResponseEntity<FileUploadResponse> uploadParentProfile(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading parent profile image: {}", file.getOriginalFilename());
        String fileUrl = s3Service.uploadFile(file, "parents/profiles");
        
        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Delete a file from S3", 
               description = "Delete a file using its full S3 URL")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Full S3 URL of the file to delete", required = true)
            @RequestParam("fileUrl") String fileUrl) {
        
        log.info("Deleting file: {}", fileUrl);
        s3Service.deleteFile(fileUrl);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/presigned-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    @Operation(summary = "Get presigned URL for file access", 
               description = "Generate a temporary presigned URL for accessing a private file")
    public ResponseEntity<String> getPresignedUrl(
            @Parameter(description = "S3 key of the file", required = true)
            @RequestParam("key") String key,
            @Parameter(description = "Duration in seconds for URL validity (default: 3600)")
            @RequestParam(value = "durationSeconds", defaultValue = "3600") int durationSeconds) {
        
        log.info("Generating presigned URL for key: {}", key);
        String presignedUrl = s3Service.getPresignedUrl(key, java.time.Duration.ofSeconds(durationSeconds));
        return ResponseEntity.ok(presignedUrl);
    }
}
