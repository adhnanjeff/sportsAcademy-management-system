package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.model.enums.AttendanceEntryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    /**
     * Entry type: REGULAR or MAKEUP
     * Defaults to REGULAR if not specified
     */
    private AttendanceEntryType entryType;

    /**
     * For MAKEUP entries: the original missed date being compensated.
     * Required when entryType is MAKEUP.
     */
    private LocalDate compensatesForDate;

    private String notes;

    /**
     * Required reason when marking attendance for a past date.
     * Mandatory for backdated entries for audit trail.
     */
    private String backdateReason;
}
