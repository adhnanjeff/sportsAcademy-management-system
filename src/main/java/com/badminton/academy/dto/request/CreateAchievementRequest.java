package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.AchievementType;
import jakarta.validation.constraints.NotBlank;
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
public class CreateAchievementRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Achievement type is required")
    private AchievementType type;

    private String eventName;
    private String position;

    @NotNull(message = "Achievement date is required")
    private LocalDate achievedDate;

    private String certificateUrl;
    private String awardedBy;
}
