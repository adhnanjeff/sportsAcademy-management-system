package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCellResponse {

    private LocalDate date;
    private AttendanceStatus status;
    private String notes;
    private Boolean marked;
    private Boolean futureDate;
}
