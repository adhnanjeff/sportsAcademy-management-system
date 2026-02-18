package com.badminton.academy.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCoachRequest extends UpdateUserRequest {

    private String specialization;
    private Integer yearsOfExperience;
    private String bio;
    private String certifications;
}
