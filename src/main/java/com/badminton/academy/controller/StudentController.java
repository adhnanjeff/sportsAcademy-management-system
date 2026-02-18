package com.badminton.academy.controller;

import com.badminton.academy.dto.request.CreateStudentRequest;
import com.badminton.academy.dto.request.UpdateStudentRequest;
import com.badminton.academy.dto.response.FeePaymentHistoryResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.dto.response.StudentResponse;
import com.badminton.academy.model.enums.SkillLevel;
import com.badminton.academy.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<StudentResponse>> getActiveStudents() {
        return ResponseEntity.ok(studentService.getActiveStudents());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#id) or @securityService.isParentOfStudent(#id)")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/skill-level/{skillLevel}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<StudentResponse>> getStudentsBySkillLevel(@PathVariable SkillLevel skillLevel) {
        return ResponseEntity.ok(studentService.getStudentsBySkillLevel(skillLevel));
    }

    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#parentId)")
    public ResponseEntity<List<StudentResponse>> getStudentsByParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(studentService.getStudentsByParent(parentId));
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<StudentResponse>> getStudentsByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(studentService.getStudentsByBatch(batchId));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<List<StudentResponse>> getStudentsByCoach(@PathVariable Long coachId) {
        return ResponseEntity.ok(studentService.getStudentsByCoach(coachId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @PostMapping("/{studentId}/batches/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<StudentResponse> assignToBatch(@PathVariable Long studentId, @PathVariable Long batchId) {
        return ResponseEntity.ok(studentService.assignToBatch(studentId, batchId));
    }

    @DeleteMapping("/{studentId}/batches/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<StudentResponse> removeFromBatch(@PathVariable Long studentId, @PathVariable Long batchId) {
        return ResponseEntity.ok(studentService.removeFromBatch(studentId, batchId));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<MessageResponse> deactivateStudent(@PathVariable Long id) {
        studentService.deactivateStudent(id);
        return ResponseEntity.ok(MessageResponse.success("Student deactivated successfully"));
    }

    @GetMapping("/count/skill-level/{skillLevel}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<Long> countBySkillLevel(@PathVariable SkillLevel skillLevel) {
        return ResponseEntity.ok(studentService.countBySkillLevel(skillLevel));
    }

    @GetMapping("/{id}/fee-history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<FeePaymentHistoryResponse>> getFeePaymentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getFeePaymentHistory(id));
    }
}
