package com.badminton.academy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTournamentRequest {

    @NotBlank(message = "Tournament name is required")
    private String name;

    @NotNull(message = "Batch is required")
    private Long batchId;

    @NotNull(message = "Participants are required")
    @Size(min = 3, message = "At least 3 participants are required")
    private List<Long> participantIds;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}
