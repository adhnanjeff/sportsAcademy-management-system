package com.badminton.academy.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OtpResponse Tests")
class OtpResponseTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("success() should create response with correct fields")
        void success_CreatesCorrectResponse() {
            // Given
            String phoneNumber = "+919876543210";
            int expiresInSeconds = 300;

            // When
            OtpResponse response = OtpResponse.success(phoneNumber, expiresInSeconds);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("OTP sent successfully");
            assertThat(response.getPhoneNumber()).isEqualTo("+919****10"); // Masked
            assertThat(response.getExpiresInSeconds()).isEqualTo(300);
            assertThat(response.getRemainingAttempts()).isNull();
        }

        @Test
        @DisplayName("error() should create response with error message")
        void error_CreatesCorrectResponse() {
            // Given
            String errorMessage = "Failed to send OTP";

            // When
            OtpResponse response = OtpResponse.error(errorMessage);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo(errorMessage);
            assertThat(response.getPhoneNumber()).isNull();
            assertThat(response.getExpiresInSeconds()).isNull();
        }

        @Test
        @DisplayName("rateLimited() should create response with retry time")
        void rateLimited_CreatesCorrectResponse() {
            // Given
            int retryAfterSeconds = 3600;

            // When
            OtpResponse response = OtpResponse.rateLimited(retryAfterSeconds);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("Too many OTP requests. Please try again later.");
            assertThat(response.getExpiresInSeconds()).isEqualTo(3600);
        }
    }

    @Nested
    @DisplayName("Phone Number Masking Tests")
    class PhoneMaskingTests {

        @Test
        @DisplayName("Should mask middle digits of phone number")
        void maskPhoneNumber_MasksMiddleDigits() {
            // Given
            String phoneNumber = "+919876543210";

            // When
            OtpResponse response = OtpResponse.success(phoneNumber, 300);

            // Then - Shows first 4 and last 2 characters
            assertThat(response.getPhoneNumber()).isEqualTo("+919****10");
        }

        @Test
        @DisplayName("Should handle short phone numbers")
        void maskPhoneNumber_ShortNumber() {
            // Given
            String phoneNumber = "+12345";

            // When
            OtpResponse response = OtpResponse.success(phoneNumber, 300);

            // Then - Short numbers are not masked
            assertThat(response.getPhoneNumber()).isEqualTo("+12345");
        }

        @Test
        @DisplayName("Should handle null phone number")
        void maskPhoneNumber_Null() {
            // When
            OtpResponse response = OtpResponse.builder()
                    .success(true)
                    .phoneNumber(null)
                    .build();

            // Then
            assertThat(response.getPhoneNumber()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder should create OtpResponse with all fields")
        void builder_AllFields() {
            // When
            OtpResponse response = OtpResponse.builder()
                    .success(true)
                    .message("Custom message")
                    .phoneNumber("+919****10")
                    .expiresInSeconds(300)
                    .remainingAttempts(3)
                    .build();

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Custom message");
            assertThat(response.getPhoneNumber()).isEqualTo("+919****10");
            assertThat(response.getExpiresInSeconds()).isEqualTo(300);
            assertThat(response.getRemainingAttempts()).isEqualTo(3);
        }
    }
}
