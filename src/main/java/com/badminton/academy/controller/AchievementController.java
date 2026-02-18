package com.badminton.academy.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<AchievementResponse> createAchievement(@Valid @RequestBody CreateAchievementRequest request) {
        AchievementResponse response = achievementService.createAchievement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AchievementResponse> updateAchievement(
            @PathVariable Long id,
            @Valid @RequestBody CreateAchievementRequest request) {
        return ResponseEntity.ok(achievementService.updateAchievement(id, request));
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAchievement(@PathVariable Long id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.ok(MessageResponse.success("Achievement deleted successfully"));
    }

    @GetMapping("/student/{studentId}/count/verified")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<Long> countVerifiedAchievements(@PathVariable Long studentId) {
        return ResponseEntity.ok(achievementService.countVerifiedAchievements(studentId));
    }
}
