package com.badminton.academy.dto.response;

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
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private Gender gender;
    private String nationalIdNumber;
    private LocalDate dateOfBirth;
    private Integer age;
    private String photoUrl;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Student-specific fields
    private SkillLevel skillLevel;
    private Set<DayOfWeek> daysOfWeek;
    private Long parentId;
    private String parentName;
    private Set<Long> batchIds;
    private Set<String> batchNames;
    private Integer totalBatches;
    private Integer totalAchievements;
    private Double attendancePercentage;
    private Double averageSkillRating;
    private BigDecimal feePayable;
    private MonthlyFeeStatus monthlyFeeStatus;
}
