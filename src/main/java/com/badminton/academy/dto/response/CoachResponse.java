package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CoachResponse extends UserResponse {

    private String specialization;
    private Integer yearsOfExperience;
    private String bio;
    private String certifications;
    private Set<Long> batchIds;
    private Integer totalBatches;
    private Integer totalStudents;
}
