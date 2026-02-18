package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long batchId;
    private String batchName;
    private LocalDate date;
    private AttendanceStatus status;
    private String notes;
    private Long markedById;
    private String markedByName;
    private LocalDateTime markedAt;
}
