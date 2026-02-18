package com.badminton.academy.dto.response;

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
public class BatchAttendanceMatrixResponse {

    private Long batchId;
    private String batchName;
    private String periodType;
    private LocalDate referenceDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate displayUntil;
    private List<AttendanceDateColumnResponse> dateColumns;
    private List<StudentAttendanceMatrixRowResponse> students;
}
