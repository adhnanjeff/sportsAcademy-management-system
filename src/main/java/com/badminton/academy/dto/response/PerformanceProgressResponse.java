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
public class PerformanceProgressResponse {
    private Long studentId;
    private String studentName;
    
    // Baseline (first evaluation)
    private Integer baselineSmashPower;
    private Integer baselineNetControl;
    private Integer baselineBackhand;
    private Integer baselineFootwork;
    private Integer baselineAgility;
    private Integer baselineStamina;
    private Integer baselineTacticalAwareness;
    private Integer baselineMentalStrength;
    private LocalDateTime baselineDate;
    private Integer baselineMonth;
    private Integer baselineYear;
    
    // Current (latest evaluation)
    private Integer currentSmashPower;
    private Integer currentNetControl;
    private Integer currentBackhand;
    private Integer currentFootwork;
    private Integer currentAgility;
    private Integer currentStamina;
    private Integer currentTacticalAwareness;
    private Integer currentMentalStrength;
    private LocalDateTime currentDate;
    private Integer currentMonth;
    private Integer currentYear;
    
    // Improvement (current - baseline)
    private Integer improvementSmashPower;
    private Integer improvementNetControl;
    private Integer improvementBackhand;
    private Integer improvementFootwork;
    private Integer improvementAgility;
    private Integer improvementStamina;
    private Integer improvementTacticalAwareness;
    private Integer improvementMentalStrength;
    
    private Double baselineAverage;
    private Double currentAverage;
    private Double improvementPercentage;
}
