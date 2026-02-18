package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.OtpChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupOtpRequest {

    @NotNull(message = "OTP channel is required")
    private OtpChannel channel;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone number must be in international format (e.g., +965XXXXXXXX)")
    private String phoneNumber;
}
