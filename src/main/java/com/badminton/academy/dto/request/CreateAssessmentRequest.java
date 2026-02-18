package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.AssessmentType;
import jakarta.validation.constraints.NotBlank;
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
public class CreateAssessmentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Assessment type is required")
    private AssessmentType type;

    @NotBlank(message = "Assessment name is required")
    private String name;

    @NotNull(message = "Score is required")
    private Double score;

    private String unit;
    private Double targetScore;

    @NotNull(message = "Assessment date is required")
    private LocalDate assessmentDate;

    private String notes;
}
