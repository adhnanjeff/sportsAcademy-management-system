package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.AchievementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String title;
    private String description;
    private AchievementType type;
    private String eventName;
    private String position;
    private LocalDate achievedDate;
    private String certificateUrl;
    private String awardedBy;
    private Boolean isVerified;
    private Long verifiedById;
    private String verifiedByName;
}
