package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {
    private Long id;
    private String name;
    private Long batchId;
    private String batchName;
    private Long createdById;
    private String createdByName;
    private TournamentStatus status;
    private Integer totalRounds;
    private Integer totalParticipants;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private List<MatchResponse> matches;
}
