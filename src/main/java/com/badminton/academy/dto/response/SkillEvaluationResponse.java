package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillEvaluationResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long evaluatedById;
    private String evaluatedByName;
    private Integer footwork;
    private Integer strokes;
    private Integer stamina;
    private Integer attack;
    private Integer defence;
    private Integer agility;
    private Integer courtCoverage;
    private String notes;
    private LocalDateTime evaluatedAt;
    private Double averageScore;
}
