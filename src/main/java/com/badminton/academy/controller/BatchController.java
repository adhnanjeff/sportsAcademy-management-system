package com.badminton.academy.controller;

import com.badminton.academy.dto.request.CreateBatchRequest;
import com.badminton.academy.dto.request.UpdateBatchRequest;
import com.badminton.academy.dto.response.BatchResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.Role;
import com.badminton.academy.model.enums.SkillLevel;
import com.badminton.academy.service.AuthService;
import com.badminton.academy.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<List<BatchResponse>> getActiveBatches() {
        return ResponseEntity.ok(batchService.getActiveBatches());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<List<BatchResponse>> getBatchesByCoach(@PathVariable Long coachId) {
        return ResponseEntity.ok(batchService.getBatchesByCoach(coachId));
    }

    @GetMapping("/coach/{coachId}/active")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<List<BatchResponse>> getActiveBatchesByCoach(@PathVariable Long coachId) {
        return ResponseEntity.ok(batchService.getActiveBatchesByCoach(coachId));
    }

    @GetMapping("/skill-level/{skillLevel}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<BatchResponse>> getBatchesBySkillLevel(@PathVariable SkillLevel skillLevel) {
        return ResponseEntity.ok(batchService.getBatchesBySkillLevel(skillLevel));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<BatchResponse>> getBatchesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(batchService.getBatchesByStudent(studentId));
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<BatchResponse>> getBatchesWithAvailableSlots() {
        return ResponseEntity.ok(batchService.getBatchesWithAvailableSlots());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<BatchResponse> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() == Role.COACH) {
            // Coaches can only create their own batches.
            request.setCoachId(currentUser.getId());
        }
        BatchResponse response = batchService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BatchResponse> updateBatch(@PathVariable Long id, @Valid @RequestBody UpdateBatchRequest request) {
        return ResponseEntity.ok(batchService.updateBatch(id, request));
    }

    @PostMapping("/{batchId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<BatchResponse> addStudentToBatch(@PathVariable Long batchId, @PathVariable Long studentId) {
        return ResponseEntity.ok(batchService.addStudentToBatch(batchId, studentId));
    }

    @DeleteMapping("/{batchId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<BatchResponse> removeStudentFromBatch(@PathVariable Long batchId, @PathVariable Long studentId) {
        return ResponseEntity.ok(batchService.removeStudentFromBatch(batchId, studentId));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deactivateBatch(@PathVariable Long id) {
        batchService.deactivateBatch(id);
        return ResponseEntity.ok(MessageResponse.success("Batch deactivated successfully"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> activateBatch(@PathVariable Long id) {
        batchService.activateBatch(id);
        return ResponseEntity.ok(MessageResponse.success("Batch activated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return ResponseEntity.ok(MessageResponse.success("Batch deleted successfully"));
    }

    @GetMapping("/{batchId}/students/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<Long> countStudentsInBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(batchService.countStudentsInBatch(batchId));
    }
}
