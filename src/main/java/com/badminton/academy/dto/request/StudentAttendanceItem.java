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
public class StudentAttendanceItem {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Status is required")
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
}
