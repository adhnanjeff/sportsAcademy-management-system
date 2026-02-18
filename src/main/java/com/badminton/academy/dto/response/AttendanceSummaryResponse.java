package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {

    private Long studentId;
    private String studentName;
    private Long totalClasses;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Long excusedCount;
    private Double attendancePercentage;
}
