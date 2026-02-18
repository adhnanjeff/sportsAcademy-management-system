package com.badminton.academy.service;

import com.badminton.academy.dto.request.BulkAttendanceRequest;
import com.badminton.academy.dto.request.MarkAttendanceRequest;
import com.badminton.academy.dto.response.AttendanceResponse;
import com.badminton.academy.dto.response.AttendanceSummaryResponse;
import java.time.LocalDate;
import java.util.List;

public interface IAttendanceService {
    AttendanceResponse markAttendance(MarkAttendanceRequest request);
    List<AttendanceResponse> markBulkAttendance(BulkAttendanceRequest request);
    AttendanceResponse getAttendanceById(Long id);
    List<AttendanceResponse> getAttendanceByStudentId(Long studentId);
    List<AttendanceResponse> getAttendanceByBatchId(Long batchId);
    List<AttendanceResponse> getAttendanceByDate(LocalDate date);
    List<AttendanceResponse> getAttendanceByBatchAndDate(Long batchId, LocalDate date);
    AttendanceSummaryResponse getAttendanceSummary(Long studentId, Long batchId);
    Double getAttendancePercentage(Long studentId, Long batchId);
    void updateAttendance(Long id, MarkAttendanceRequest request);
    void deleteAttendance(Long id);
}
