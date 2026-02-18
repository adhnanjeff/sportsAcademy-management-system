package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.Role;
import com.badminton.academy.model.enums.OtpChannel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Email(message = "Email should be valid")
    private String email;

    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String nationalIdNumber;

    private Role role;

    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private String photoUrl;
    private String parentPhoneNumber;
    private String specialization;
    private Integer yearsOfExperience;
    private String bio;
    private String certifications;

    private String otp;
    private OtpChannel otpChannel;
}
