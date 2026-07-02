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
public class HeadToHeadResponse {
    private Long student1Id;
    private String student1Name;
    private Long student2Id;
    private String student2Name;
    private Integer student1Wins;
    private Integer student2Wins;
    private Integer draws;
    private Integer totalMatches;
    private List<MatchResponse> recentMatches;
}
