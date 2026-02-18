package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    private boolean success;
    private String message;
    private String phoneNumber;
    private Integer expiresInSeconds;
    private Integer remainingAttempts;

    public static OtpResponse success(String phoneNumber, int expiresInSeconds) {
        return OtpResponse.builder()
                .success(true)
                .message("OTP sent successfully")
                .phoneNumber(maskPhoneNumber(phoneNumber))
                .expiresInSeconds(expiresInSeconds)
                .build();
    }

    public static OtpResponse error(String message) {
        return OtpResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static OtpResponse rateLimited(int retryAfterSeconds) {
        return OtpResponse.builder()
                .success(false)
                .message("Too many OTP requests. Please try again later.")
                .expiresInSeconds(retryAfterSeconds)
                .build();
    }

    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return phoneNumber;
        }
        // Show first 4 and last 2 characters
        int length = phoneNumber.length();
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(length - 2);
    }
}
