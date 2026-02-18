package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBatchRequest {

    private String name;
    private SkillLevel skillLevel;
    private Long coachId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
}
