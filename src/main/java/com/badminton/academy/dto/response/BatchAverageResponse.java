package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAverageResponse {
    private Long batchId;
    private String batchName;
    private Integer totalPlayers;
    
    // 8-Axis Average Metrics
    private Double avgSmashPower;
    private Double avgNetControl;
    private Double avgBackhand;
    private Double avgFootwork;
    private Double avgAgility;
    private Double avgStamina;
    private Double avgTacticalAwareness;
    private Double avgMentalStrength;
    
    private Double overallAverage;
}
