package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.model.enums.AttendanceEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for attendance audit log entries.
 * Used for viewing attendance modification history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAuditLogResponse {

    private Long id;
    private Long attendanceId;
    
    // Attendance context
    private Long studentId;
    private String studentName;
    private Long batchId;
    private String batchName;
    private LocalDate attendanceDate;
    
    // Action details
    private String action; // CREATE, UPDATE, DELETE
    
    // Previous values
    private AttendanceStatus previousStatus;
    private AttendanceEntryType previousEntryType;
    private String previousNotes;
    
    // New values
    private AttendanceStatus newStatus;
    private AttendanceEntryType newEntryType;
    private String newNotes;
    
    // Who made the change
    private Long changedById;
    private String changedByName;
    private String changedByRole;
    
    // Audit details
    private String reason;
    private Boolean wasBackdated;
    private LocalDateTime changedAt;
}
