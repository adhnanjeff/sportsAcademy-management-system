package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.SkillLevel;
import com.badminton.academy.model.enums.MonthlyFeeStatus;
import com.badminton.academy.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequest {

    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String nationalIdNumber;
    private String phoneNumber;
    private String photoUrl;
    private String address;
    private String city;
    private String state;
    private String country;
    
    private SkillLevel skillLevel;
    private Set<DayOfWeek> daysOfWeek;
    private Long parentId;
    private BigDecimal feePayable;
    private MonthlyFeeStatus monthlyFeeStatus;
}
