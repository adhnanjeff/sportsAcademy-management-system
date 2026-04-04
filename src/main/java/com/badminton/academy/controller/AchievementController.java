package com.badminton.academy.controller;

import com.badminton.academy.annotation.RateLimit;
import com.badminton.academy.dto.request.CreateAchievementRequest;
import com.badminton.academy.dto.response.AchievementResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.AchievementType;
import com.badminton.academy.service.AchievementService;
import com.badminton.academy.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AchievementResponse>> getAllAchievements() {
        return ResponseEntity.ok(achievementService.getAllAchievements());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<AchievementResponse> getAchievementById(@PathVariable Long id) {
        return ResponseEntity.ok(achievementService.getAchievementById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<AchievementResponse>> getAchievementsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(achievementService.getAchievementsByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/verified")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<AchievementResponse>> getVerifiedAchievementsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(achievementService.getVerifiedAchievementsByStudent(studentId));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AchievementResponse>> getAchievementsByType(@PathVariable AchievementType type) {
        return ResponseEntity.ok(achievementService.getAchievementsByType(type));
    }

    @GetMapping("/student/{studentId}/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<List<AchievementResponse>> getAchievementsByStudentAndType(
            @PathVariable Long studentId,
            @PathVariable AchievementType type) {
        return ResponseEntity.ok(achievementService.getAchievementsByStudentAndType(studentId, type));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AchievementResponse>> getAchievementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(achievementService.getAchievementsByDateRange(startDate, endDate));
    }

    @GetMapping("/pending-verification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AchievementResponse>> getPendingVerificationAchievements() {
        return ResponseEntity.ok(achievementService.getPendingVerificationAchievements());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<AchievementResponse> createAchievement(
            @Valid @RequestPart("achievement") CreateAchievementRequest request,
            @RequestPart(value = "certificate", required = false) MultipartFile certificate) {
        AchievementResponse response = achievementService.createAchievement(request, certificate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AchievementResponse> updateAchievement(
            @PathVariable Long id,
            @Valid @RequestPart("achievement") CreateAchievementRequest request,
            @RequestPart(value = "certificate", required = false) MultipartFile certificate) {
        return ResponseEntity.ok(achievementService.updateAchievement(id, request, certificate));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AchievementResponse> verifyAchievement(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(achievementService.verifyAchievement(id, currentUser.getId()));
    }

    @PutMapping("/{id}/unverify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementResponse> unverifyAchievement(@PathVariable Long id) {
        return ResponseEntity.ok(achievementService.unverifyAchievement(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<MessageResponse> deleteAchievement(@PathVariable Long id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.ok(MessageResponse.success("Achievement deleted successfully"));
    }

    @DeleteMapping("/{id}/certificate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<MessageResponse> deleteCertificate(@PathVariable Long id) {
        achievementService.deleteCertificate(id);
        return ResponseEntity.ok(MessageResponse.success("Certificate deleted successfully"));
    }

    @GetMapping("/student/{studentId}/count/verified")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<Long> countVerifiedAchievements(@PathVariable Long studentId) {
        return ResponseEntity.ok(achievementService.countVerifiedAchievements(studentId));
    }

    // Bulk Operations

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    @RateLimit(maxRequests = 3, windowSeconds = 60)
    public ResponseEntity<MessageResponse> bulkDeleteAchievements(@RequestBody BulkIdsRequest request) {
        User currentUser = authService.getCurrentUser();
        achievementService.bulkDeleteAchievements(request.getIds(), currentUser);
        return ResponseEntity.ok(MessageResponse.success(
            String.format("Successfully deleted %d achievement(s)", request.getIds().size())
        ));
    }

    @PostMapping("/bulk-verify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    @RateLimit(maxRequests = 5, windowSeconds = 60)
    public ResponseEntity<MessageResponse> bulkVerifyAchievements(@RequestBody BulkVerifyRequest request) {
        User currentUser = authService.getCurrentUser();
        achievementService.bulkVerifyAchievements(request.getIds(), request.isVerified(), currentUser);
        return ResponseEntity.ok(MessageResponse.success(
            String.format("Successfully %s %d achievement(s)", 
                request.isVerified() ? "verified" : "unverified",
                request.getIds().size())
        ));
    }

    // DTO Classes for bulk operations

    @lombok.Data
    public static class BulkIdsRequest {
        private List<Long> ids;
    }

    @lombok.Data
    public static class BulkVerifyRequest {
        private List<Long> ids;
        private boolean verified;
    }
}
