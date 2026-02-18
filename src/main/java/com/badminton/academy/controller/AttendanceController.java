package com.badminton.academy.controller;

import com.badminton.academy.dto.request.BulkAttendanceRequest;
import com.badminton.academy.dto.request.MarkAttendanceRequest;
import com.badminton.academy.dto.response.AttendanceResponse;
import com.badminton.academy.dto.response.AttendanceSummaryResponse;
import com.badminton.academy.dto.response.BatchAttendanceMatrixResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.model.User;
import com.badminton.academy.service.AttendanceService;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendances() {
        return ResponseEntity.ok(attendanceService.getAllAttendances());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AttendanceResponse> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByStudent(studentId));
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByBatch(batchId));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    @GetMapping("/student/{studentId}/range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByStudentAndDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceByStudentAndDateRange(studentId, startDate, endDate));
    }

    @GetMapping("/batch/{batchId}/range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByBatchAndDateRange(
            @PathVariable Long batchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceByBatchAndDateRange(batchId, startDate, endDate));
    }

    @GetMapping("/batch/{batchId}/weekly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<BatchAttendanceMatrixResponse> getBatchWeeklyAttendance(
            @PathVariable Long batchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return ResponseEntity.ok(attendanceService.getBatchWeeklyAttendance(batchId, referenceDate));
    }

    @GetMapping("/batch/{batchId}/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<BatchAttendanceMatrixResponse> getBatchMonthlyAttendance(
            @PathVariable Long batchId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return ResponseEntity.ok(attendanceService.getBatchMonthlyAttendance(batchId, year, month, referenceDate));
    }

    @GetMapping("/coach/{coachId}/date/{date}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#coachId)")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByCoachAndDate(
            @PathVariable Long coachId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByCoachAndDate(coachId, date));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AttendanceResponse> markAttendance(@Valid @RequestBody MarkAttendanceRequest request) {
        User currentUser = authService.getCurrentUser();
        AttendanceResponse response = attendanceService.markAttendance(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<AttendanceResponse>> markBulkAttendance(@Valid @RequestBody BulkAttendanceRequest request) {
        User currentUser = authService.getCurrentUser();
        List<AttendanceResponse> responses = attendanceService.markBulkAttendance(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody MarkAttendanceRequest request) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(MessageResponse.success("Attendance record deleted successfully"));
    }

    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<AttendanceSummaryResponse> getStudentAttendanceSummary(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceSummary(studentId));
    }

    @GetMapping("/student/{studentId}/batch/{batchId}/percentage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<Double> calculateAttendancePercentage(
            @PathVariable Long studentId,
            @PathVariable Long batchId) {
        return ResponseEntity.ok(attendanceService.calculateAttendancePercentage(studentId, batchId));
    }
}
