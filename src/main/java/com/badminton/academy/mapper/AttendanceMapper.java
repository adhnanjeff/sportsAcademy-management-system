package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.MarkAttendanceRequest;
import com.badminton.academy.dto.response.AttendanceResponse;
import com.badminton.academy.dto.response.AttendanceSummaryResponse;
import com.badminton.academy.model.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public AttendanceResponse toResponse(Attendance attendance) {
        if (attendance == null) return null;

        AttendanceResponse response = new AttendanceResponse();
        response.setId(attendance.getId());
        response.setDate(attendance.getDate());
        response.setStatus(attendance.getStatus());
        response.setNotes(attendance.getNotes());
        response.setMarkedAt(attendance.getMarkedAt());

        if (attendance.getStudent() != null) {
            response.setStudentId(attendance.getStudent().getId());
            response.setStudentName(attendance.getStudent().getFullName());
        }

        if (attendance.getBatch() != null) {
            response.setBatchId(attendance.getBatch().getId());
            response.setBatchName(attendance.getBatch().getName());
        }

        if (attendance.getMarkedBy() != null) {
            response.setMarkedById(attendance.getMarkedBy().getId());
            response.setMarkedByName(attendance.getMarkedBy().getFullName());
        }

        return response;
    }

    public Attendance toEntity(MarkAttendanceRequest request) {
        if (request == null) return null;

        Attendance attendance = new Attendance();
        attendance.setDate(request.getDate());
        attendance.setStatus(request.getStatus());
        attendance.setNotes(request.getNotes());
        return attendance;
    }

    public void updateEntityFromRequest(MarkAttendanceRequest request, Attendance attendance) {
        if (request == null || attendance == null) return;

        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getNotes() != null) attendance.setNotes(request.getNotes());
    }

    public AttendanceSummaryResponse toSummaryResponse(
            Long studentId,
            String studentName,
            Long totalClasses,
            Long presentCount,
            Long absentCount,
            Long lateCount,
            Long excusedCount,
            Double attendancePercentage) {
        return AttendanceSummaryResponse.builder()
                .studentId(studentId)
                .studentName(studentName)
                .totalClasses(totalClasses)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .excusedCount(excusedCount)
                .attendancePercentage(attendancePercentage)
                .build();
    }
}
