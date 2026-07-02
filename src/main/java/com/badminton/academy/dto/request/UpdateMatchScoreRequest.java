package com.badminton.academy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMatchScoreRequest {

    @NotNull(message = "Winner is required")
    private Long winnerId;

    @NotBlank(message = "Player 1 score is required")
    private String player1Score;

    @NotBlank(message = "Player 2 score is required")
    private String player2Score;

    @NotBlank(message = "Score display is required")
    private String scoreDisplay;

    private Integer durationMinutes;
    private String notes;
}
