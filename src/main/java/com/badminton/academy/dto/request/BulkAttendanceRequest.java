package com.badminton.academy.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotEmpty(message = "Student attendance list cannot be empty")
    @Valid
    private List<StudentAttendanceItem> studentAttendances;

    /**
     * Required reason when marking attendance for a past date.
     * Mandatory for backdated entries for audit trail.
     */
    private String backdateReason;
}
