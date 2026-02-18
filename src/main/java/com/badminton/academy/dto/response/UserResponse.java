package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String nationalIdNumber;
    private LocalDate dateOfBirth;
    private Integer age;
    private String photoUrl;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private Role role;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
