package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {

    private Long id;
    private String name;
    private SkillLevel skillLevel;
    private Long coachId;
    private String coachName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
    private Integer totalStudents;
    private Set<Long> studentIds;
}
