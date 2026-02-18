package com.badminton.academy.controller;

import com.badminton.academy.dto.request.OtpRequestDto;
import com.badminton.academy.dto.request.OtpVerifyDto;
import com.badminton.academy.dto.response.AuthResponse;
import com.badminton.academy.dto.response.OtpResponse;
import com.badminton.academy.dto.response.UserResponse;
import com.badminton.academy.model.enums.Role;
import com.badminton.academy.service.AuthService;
import com.badminton.academy.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController OTP endpoints.
 * Tests the controller layer in isolation with mocked services.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController OTP Endpoint Tests")
class AuthControllerOtpTest {

    @Mock
    private AuthService authService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthController authController;

    private static final String VALID_PHONE = "+919876543210";
    private static final String VALID_OTP = "123456";

    @Nested
    @DisplayName("POST /api/auth/otp/request")
    class RequestOtpEndpoint {

        @Test
        @DisplayName("Should return 200 OK with success response when OTP is sent")
        void requestOtp_Success() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            OtpResponse otpResponse = OtpResponse.success(VALID_PHONE, 300);
            when(otpService.requestOtp(any(OtpRequestDto.class))).thenReturn(otpResponse);

            // When
            ResponseEntity<OtpResponse> response = authController.requestOtp(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("OTP sent successfully");
            assertThat(response.getBody().getExpiresInSeconds()).isEqualTo(300);
            verify(otpService).requestOtp(any(OtpRequestDto.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when rate limited")
        void requestOtp_RateLimited() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            OtpResponse otpResponse = OtpResponse.rateLimited(3600);
            when(otpService.requestOtp(any(OtpRequestDto.class))).thenReturn(otpResponse);

            // When
            ResponseEntity<OtpResponse> response = authController.requestOtp(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("Too many OTP requests");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when SMS fails")
        void requestOtp_SmsFailure() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            OtpResponse otpResponse = OtpResponse.error("Failed to send OTP. Please try again.");
            when(otpService.requestOtp(any(OtpRequestDto.class))).thenReturn(otpResponse);

            // When
            ResponseEntity<OtpResponse> response = authController.requestOtp(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("Failed to send OTP");
        }
    }

    @Nested
    @DisplayName("POST /api/auth/otp/verify")
    class VerifyOtpEndpoint {

        @Test
        @DisplayName("Should return 200 OK with auth response on successful verification")
        void verifyOtp_Success() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            UserResponse userResponse = UserResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .firstName("Test")
                    .lastName("User")
                    .fullName("Test User")
                    .phoneNumber(VALID_PHONE)
                    .role(Role.PARENT)
                    .isActive(true)
                    .build();

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken("access_token")
                    .refreshToken("refresh_token")
                    .tokenType("Bearer")
                    .expiresIn(86400000L)
                    .user(userResponse)
                    .build();

            when(otpService.verifyOtp(any(OtpVerifyDto.class))).thenReturn(authResponse);

            // When
            ResponseEntity<AuthResponse> response = authController.verifyOtp(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAccessToken()).isEqualTo("access_token");
            assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh_token");
            assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
            assertThat(response.getBody().getUser()).isNotNull();
            assertThat(response.getBody().getUser().getEmail()).isEqualTo("test@example.com");
            assertThat(response.getBody().getUser().getRole()).isEqualTo(Role.PARENT);
            verify(otpService).verifyOtp(any(OtpVerifyDto.class));
        }

        @Test
        @DisplayName("Should propagate exception when OTP is invalid")
        void verifyOtp_InvalidOtp() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            when(otpService.verifyOtp(any(OtpVerifyDto.class)))
                    .thenThrow(new IllegalArgumentException("Invalid OTP. 4 attempts remaining."));

            // When/Then
            assertThatThrownBy(() -> authController.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid OTP");
            verify(otpService).verifyOtp(any(OtpVerifyDto.class));
        }

        @Test
        @DisplayName("Should propagate exception when OTP is expired")
        void verifyOtp_ExpiredOtp() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            when(otpService.verifyOtp(any(OtpVerifyDto.class)))
                    .thenThrow(new IllegalArgumentException("Invalid or expired OTP. Please request a new one."));

            // When/Then
            assertThatThrownBy(() -> authController.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("expired OTP");
        }

        @Test
        @DisplayName("Should propagate exception when max attempts exceeded")
        void verifyOtp_MaxAttemptsExceeded() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            when(otpService.verifyOtp(any(OtpVerifyDto.class)))
                    .thenThrow(new IllegalArgumentException("Too many failed attempts. Please request a new OTP."));

            // When/Then
            assertThatThrownBy(() -> authController.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Too many failed attempts");
        }
    }
}
