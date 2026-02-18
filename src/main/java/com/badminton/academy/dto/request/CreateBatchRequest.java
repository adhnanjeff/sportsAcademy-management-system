package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchRequest {

    @NotBlank(message = "Batch name is required")
    private String name;

    @NotNull(message = "Skill level is required")
    private SkillLevel skillLevel;

    @NotNull(message = "Coach ID is required")
    private Long coachId;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
