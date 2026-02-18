package com.badminton.academy.controller;

import com.badminton.academy.dto.request.CreateAssessmentRequest;
import com.badminton.academy.dto.response.AssessmentResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.AssessmentType;
import com.badminton.academy.service.AssessmentService;
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
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByStudentOrderByDate(@PathVariable Long studentId) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByStudentOrderByDate(studentId));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByCoach(@PathVariable Long coachId) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByCoach(coachId));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByType(@PathVariable AssessmentType type) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByType(type));
    }

    @GetMapping("/student/{studentId}/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByStudentAndType(
            @PathVariable Long studentId,
            @PathVariable AssessmentType type) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByStudentAndType(studentId, type));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByDateRange(startDate, endDate));
    }

    @GetMapping("/student/{studentId}/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentsByStudentAndDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(assessmentService.getAssessmentsByStudentAndDateRange(studentId, startDate, endDate));
    }

    @GetMapping("/student/{studentId}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentProgress(
            @PathVariable Long studentId,
            @RequestParam String assessmentName) {
        return ResponseEntity.ok(assessmentService.getAssessmentProgress(studentId, assessmentName));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AssessmentResponse> createAssessment(@Valid @RequestBody CreateAssessmentRequest request) {
        User currentUser = authService.getCurrentUser();
        AssessmentResponse response = assessmentService.createAssessment(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody CreateAssessmentRequest request) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(assessmentService.updateAssessment(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAssessment(@PathVariable Long id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.ok(MessageResponse.success("Assessment deleted successfully"));
    }

    @GetMapping("/coach/{coachId}/count")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<Long> countByCoach(@PathVariable Long coachId) {
        return ResponseEntity.ok(assessmentService.countByCoach(coachId));
    }
}
