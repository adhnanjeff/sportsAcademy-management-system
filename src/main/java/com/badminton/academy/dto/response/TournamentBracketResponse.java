package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentBracketResponse {
    private Long tournamentId;
    private String tournamentName;
    private TournamentStatus status;
    private Integer totalRounds;
    private List<BracketRound> rounds;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BracketRound {
        private Integer roundNumber;
        private String roundName;
        private List<MatchResponse> matches;
    }
}
