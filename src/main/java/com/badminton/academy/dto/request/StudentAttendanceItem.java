package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceItem {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String notes;
}
