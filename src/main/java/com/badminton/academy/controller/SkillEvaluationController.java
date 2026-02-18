package com.badminton.academy.controller;

import com.badminton.academy.dto.request.CreateSkillEvaluationRequest;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.dto.response.SkillEvaluationResponse;
import com.badminton.academy.model.User;
import com.badminton.academy.service.AuthService;
import com.badminton.academy.service.SkillEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/skill-evaluations")
@RequiredArgsConstructor
public class SkillEvaluationController {

    private final SkillEvaluationService skillEvaluationService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<SkillEvaluationResponse>> getAllSkillEvaluations() {
        return ResponseEntity.ok(skillEvaluationService.getAllSkillEvaluations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<SkillEvaluationResponse> getSkillEvaluationById(@PathVariable Long id) {
        return ResponseEntity.ok(skillEvaluationService.getSkillEvaluationById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<SkillEvaluationResponse>> getSkillEvaluationsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(skillEvaluationService.getSkillEvaluationsByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<SkillEvaluationResponse>> getSkillEvaluationsByStudentOrderByDate(@PathVariable Long studentId) {
        return ResponseEntity.ok(skillEvaluationService.getSkillEvaluationsByStudentOrderByDate(studentId));
    }

    @GetMapping("/student/{studentId}/latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<SkillEvaluationResponse> getLatestSkillEvaluation(@PathVariable Long studentId) {
        Optional<SkillEvaluationResponse> response = skillEvaluationService.getLatestSkillEvaluation(studentId);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<List<SkillEvaluationResponse>> getSkillEvaluationsByCoach(@PathVariable Long coachId) {
        return ResponseEntity.ok(skillEvaluationService.getSkillEvaluationsByCoach(coachId));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<SkillEvaluationResponse>> getSkillEvaluationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(skillEvaluationService.getSkillEvaluationsByDateRange(startDate, endDate));
    }

    @GetMapping("/student/{studentId}/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<List<SkillEvaluationResponse>> getSkillEvaluationsByStudentAndDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(skillEvaluationService.getSkillEvaluationsByStudentAndDateRange(studentId, startDate, endDate));
    }

    @GetMapping("/coach/{coachId}/recent")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<List<SkillEvaluationResponse>> getRecentEvaluationsByCoach(
            @PathVariable Long coachId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ResponseEntity.ok(skillEvaluationService.getRecentEvaluationsByCoach(coachId, since));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<SkillEvaluationResponse> createSkillEvaluation(@Valid @RequestBody CreateSkillEvaluationRequest request) {
        User currentUser = authService.getCurrentUser();
        SkillEvaluationResponse response = skillEvaluationService.createSkillEvaluation(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<SkillEvaluationResponse> updateSkillEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody CreateSkillEvaluationRequest request) {
        return ResponseEntity.ok(skillEvaluationService.updateSkillEvaluation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteSkillEvaluation(@PathVariable Long id) {
        skillEvaluationService.deleteSkillEvaluation(id);
        return ResponseEntity.ok(MessageResponse.success("Skill evaluation deleted successfully"));
    }

    @GetMapping("/student/{studentId}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<Long> countByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(skillEvaluationService.countByStudent(studentId));
    }

    @GetMapping("/coach/{coachId}/count")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<Long> countByCoach(@PathVariable Long coachId) {
        return ResponseEntity.ok(skillEvaluationService.countByCoach(coachId));
    }

    @GetMapping("/student/{studentId}/average-score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<Double> getAverageScore(@PathVariable Long studentId) {
        return ResponseEntity.ok(skillEvaluationService.getAverageScore(studentId));
    }
}
