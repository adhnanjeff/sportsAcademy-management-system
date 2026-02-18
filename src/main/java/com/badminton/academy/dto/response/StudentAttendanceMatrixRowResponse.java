package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceMatrixRowResponse {

    private Long studentId;
    private String studentName;
    private List<AttendanceCellResponse> attendance;
}
