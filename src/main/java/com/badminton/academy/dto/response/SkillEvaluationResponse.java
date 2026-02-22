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
    private Long batchId;
    private String batchName;
    private Long evaluatedById;
    private String evaluatedByName;
    
    // 8-Axis Performance Metrics
    private Integer smashPower;
    private Integer netControl;
    private Integer backhand;
    private Integer footwork;
    private Integer agility;
    private Integer stamina;
    private Integer tacticalAwareness;
    private Integer mentalStrength;
    
    private String notes;
    private LocalDateTime evaluatedAt;
    private Integer month;
    private Integer year;
    private Double averageScore;
}
