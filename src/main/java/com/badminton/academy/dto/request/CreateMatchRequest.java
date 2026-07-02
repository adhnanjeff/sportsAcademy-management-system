package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.MatchType;
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
public class CreateMatchRequest {

    @NotNull(message = "Player 1 is required")
    private Long player1Id;

    private Long player2Id;

    @NotNull(message = "Match type is required")
    private MatchType matchType;

    private String player1Score;
    private String player2Score;
    private String scoreDisplay;
    private Long winnerId;

    private Long partner1Id;
    private Long partner2Id;

    @NotNull(message = "Match date is required")
    private LocalDate matchDate;

    private Integer durationMinutes;
    private String notes;
    private String location;
    private String eventName;
}
