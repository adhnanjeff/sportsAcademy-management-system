package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryResponse {
    private Integer rank;
    private Long studentId;
    private String studentName;
    private String photoUrl;
    private Integer matchWins;
    private Integer matchTotal;
    private Integer winRate;
    private Double assessmentAvg;
    private Double attendancePercentage;
    private Double compositeScore;
}
