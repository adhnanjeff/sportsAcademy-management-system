package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.AssessmentType;
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
public class AssessmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long conductedById;
    private String conductedByName;
    private AssessmentType type;
    private String name;
    private Double score;
    private String unit;
    private Double targetScore;
    private LocalDate assessmentDate;
    private String notes;
    private LocalDateTime createdAt;
    private Boolean targetAchieved;
}
