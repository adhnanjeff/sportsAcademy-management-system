package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.MatchType;
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
public class MatchResponse {
    private Long id;
    private Long player1Id;
    private String player1Name;
    private Long player2Id;
    private String player2Name;
    private Long winnerId;
    private String winnerName;
    private MatchType matchType;
    private String player1Score;
    private String player2Score;
    private String scoreDisplay;
    private Long tournamentId;
    private Integer roundNumber;
    private Integer matchPosition;
    private Long partner1Id;
    private String partner1Name;
    private Long partner2Id;
    private String partner2Name;
    private LocalDate matchDate;
    private Integer durationMinutes;
    private String notes;
    private String location;
    private String eventName;
    private LocalDateTime createdAt;
}
