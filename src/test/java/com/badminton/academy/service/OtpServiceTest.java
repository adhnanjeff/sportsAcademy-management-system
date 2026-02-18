package com.badminton.academy.service;

import com.badminton.academy.dto.request.OtpRequestDto;
import com.badminton.academy.dto.request.OtpVerifyDto;
import com.badminton.academy.dto.response.AuthResponse;
import com.badminton.academy.dto.response.OtpResponse;
import com.badminton.academy.model.OtpVerification;
import com.badminton.academy.model.Parent;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.Role;
import com.badminton.academy.repository.OtpVerificationRepository;
import com.badminton.academy.repository.ParentRepository;
import com.badminton.academy.repository.UserRepository;
import com.badminton.academy.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Tests")
class OtpServiceTest {

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SmsService smsService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private OtpService otpService;

    private static final String VALID_PHONE = "+919876543210";
    private static final String VALID_OTP = "123456";
    private static final String ENCODED_OTP = "encoded_otp_hash";

    @BeforeEach
    void setUp() {
        // Set configuration values via reflection
        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 5);
        ReflectionTestUtils.setField(otpService, "maxRequestsPerHour", 5);
        ReflectionTestUtils.setField(otpService, "maxVerifyAttempts", 5);
    }

    @Nested
    @DisplayName("Request OTP Tests")
    class RequestOtpTests {

        @Test
        @DisplayName("Should successfully request OTP when rate limit not exceeded")
        void requestOtp_Success() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            when(otpRepository.countRecentOtpRequests(anyString(), any(LocalDateTime.class)))
                    .thenReturn(0L);
            when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_OTP);
            when(smsService.sendOtp(anyString(), anyString())).thenReturn(true);

            // When
            OtpResponse response = otpService.requestOtp(request);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("OTP sent successfully");
            assertThat(response.getExpiresInSeconds()).isEqualTo(300); // 5 minutes

            verify(otpRepository).invalidateAllOtpsForPhoneNumber(VALID_PHONE);
            verify(otpRepository).save(any(OtpVerification.class));
            verify(smsService).sendOtp(eq(VALID_PHONE), anyString());
        }

        @Test
        @DisplayName("Should return rate limited response when too many requests")
        void requestOtp_RateLimited() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            when(otpRepository.countRecentOtpRequests(anyString(), any(LocalDateTime.class)))
                    .thenReturn(5L); // Max requests reached

            // When
            OtpResponse response = otpService.requestOtp(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("Too many OTP requests");
            assertThat(response.getExpiresInSeconds()).isEqualTo(3600); // 1 hour

            verify(otpRepository, never()).save(any(OtpVerification.class));
            verify(smsService, never()).sendOtp(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return error when SMS sending fails")
        void requestOtp_SmsFailure() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            when(otpRepository.countRecentOtpRequests(anyString(), any(LocalDateTime.class)))
                    .thenReturn(0L);
            when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_OTP);
            when(smsService.sendOtp(anyString(), anyString())).thenReturn(false);

            // When
            OtpResponse response = otpService.requestOtp(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("Failed to send OTP");

            verify(otpRepository).save(any(OtpVerification.class));
        }

        @Test
        @DisplayName("Should normalize phone number by removing spaces and dashes")
        void requestOtp_NormalizesPhoneNumber() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber("+91 987-654-3210")
                    .build();

            when(otpRepository.countRecentOtpRequests(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(0L);
            when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_OTP);
            when(smsService.sendOtp(eq(VALID_PHONE), anyString())).thenReturn(true);

            // When
            OtpResponse response = otpService.requestOtp(request);

            // Then
            assertThat(response.isSuccess()).isTrue();
            verify(otpRepository).invalidateAllOtpsForPhoneNumber(VALID_PHONE);
        }

        @Test
        @DisplayName("Should invalidate previous OTPs for same phone number")
        void requestOtp_InvalidatesPreviousOtps() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            when(otpRepository.countRecentOtpRequests(anyString(), any(LocalDateTime.class)))
                    .thenReturn(0L);
            when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_OTP);
            when(smsService.sendOtp(anyString(), anyString())).thenReturn(true);

            // When
            otpService.requestOtp(request);

            // Then
            verify(otpRepository).invalidateAllOtpsForPhoneNumber(VALID_PHONE);
        }

        @Test
        @DisplayName("Should create OTP with correct expiry time")
        void requestOtp_CorrectExpiryTime() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .build();

            when(otpRepository.countRecentOtpRequests(anyString(), any(LocalDateTime.class)))
                    .thenReturn(0L);
            when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_OTP);
            when(smsService.sendOtp(anyString(), anyString())).thenReturn(true);

            ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);

            // When
            otpService.requestOtp(request);

            // Then
            verify(otpRepository).save(otpCaptor.capture());
            OtpVerification savedOtp = otpCaptor.getValue();

            assertThat(savedOtp.getPhoneNumber()).isEqualTo(VALID_PHONE);
            assertThat(savedOtp.getOtpHash()).isEqualTo(ENCODED_OTP);
            assertThat(savedOtp.getIsUsed()).isFalse();
            assertThat(savedOtp.getAttemptCount()).isEqualTo(0);
            assertThat(savedOtp.getExpiresAt()).isAfter(LocalDateTime.now());
            assertThat(savedOtp.getExpiresAt()).isBefore(LocalDateTime.now().plusMinutes(6));
        }
    }

    @Nested
    @DisplayName("Verify OTP Tests")
    class VerifyOtpTests {

        @Test
        @DisplayName("Should successfully verify OTP and return auth response for existing user")
        void verifyOtp_ExistingUser_Success() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            OtpVerification otp = createValidOtpVerification();

            User existingUser = createTestUser();

            when(otpRepository.findLatestValidOtp(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(otp));
            when(passwordEncoder.matches(VALID_OTP, ENCODED_OTP)).thenReturn(true);
            when(userRepository.findByPhoneNumber(VALID_PHONE)).thenReturn(Optional.of(existingUser));
            when(jwtUtils.generateTokenFromUsername(anyString())).thenReturn("access_token");
            when(jwtUtils.generateRefreshToken(anyString())).thenReturn("refresh_token");

            // When
            AuthResponse response = otpService.verifyOtp(request);

            // Then
            assertThat(response.getAccessToken()).isEqualTo("access_token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo(existingUser.getEmail());

            verify(otpRepository).save(argThat(o -> o.getIsUsed()));
        }

        @Test
        @DisplayName("Should create new Parent user when phone number not found")
        void verifyOtp_NewUser_CreatesParent() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            OtpVerification otp = createValidOtpVerification();

            Parent newParent = createTestParent();

            when(otpRepository.findLatestValidOtp(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(otp));
            when(passwordEncoder.matches(VALID_OTP, ENCODED_OTP)).thenReturn(true);
            when(userRepository.findByPhoneNumber(VALID_PHONE)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
            when(parentRepository.save(any(Parent.class))).thenReturn(newParent);
            when(jwtUtils.generateTokenFromUsername(anyString())).thenReturn("access_token");
            when(jwtUtils.generateRefreshToken(anyString())).thenReturn("refresh_token");

            // When
            AuthResponse response = otpService.verifyOtp(request);

            // Then
            assertThat(response.getAccessToken()).isEqualTo("access_token");
            verify(parentRepository).save(any(Parent.class));
        }

        @Test
        @DisplayName("Should throw exception when no valid OTP found")
        void verifyOtp_NoValidOtp_ThrowsException() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            when(otpRepository.findLatestValidOtp(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> otpService.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid or expired OTP");
        }

        @Test
        @DisplayName("Should throw exception when OTP is incorrect")
        void verifyOtp_InvalidOtp_ThrowsException() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp("000000")
                    .build();

            OtpVerification otp = createValidOtpVerification();

            when(otpRepository.findLatestValidOtp(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(otp));
            when(passwordEncoder.matches("000000", ENCODED_OTP)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> otpService.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid OTP");

            verify(otpRepository).save(argThat(o -> o.getAttemptCount() == 1));
        }

        @Test
        @DisplayName("Should throw exception when max attempts exceeded")
        void verifyOtp_MaxAttemptsExceeded_ThrowsException() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            OtpVerification otp = createValidOtpVerification();
            otp.setAttemptCount(5); // Max attempts reached

            when(otpRepository.findLatestValidOtp(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(otp));

            // When/Then
            assertThatThrownBy(() -> otpService.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Too many failed attempts");

            verify(otpRepository).save(argThat(o -> o.getIsUsed()));
        }

        @Test
        @DisplayName("Should increment attempt count on invalid OTP")
        void verifyOtp_IncrementAttemptCount() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp("000000")
                    .build();

            OtpVerification otp = createValidOtpVerification();
            otp.setAttemptCount(2);

            when(otpRepository.findLatestValidOtp(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(otp));
            when(passwordEncoder.matches("000000", ENCODED_OTP)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> otpService.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2 attempts remaining");

            verify(otpRepository).save(argThat(o -> o.getAttemptCount() == 3));
        }

        @Test
        @DisplayName("Should throw exception when user account is deactivated")
        void verifyOtp_DeactivatedUser_ThrowsException() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(VALID_PHONE)
                    .otp(VALID_OTP)
                    .build();

            OtpVerification otp = createValidOtpVerification();

            User deactivatedUser = createTestUser();
            deactivatedUser.setIsActive(false);

            when(otpRepository.findLatestValidOtp(eq(VALID_PHONE), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(otp));
            when(passwordEncoder.matches(VALID_OTP, ENCODED_OTP)).thenReturn(true);
            when(userRepository.findByPhoneNumber(VALID_PHONE)).thenReturn(Optional.of(deactivatedUser));

            // When/Then
            assertThatThrownBy(() -> otpService.verifyOtp(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account is deactivated");
        }
    }

    @Nested
    @DisplayName("Cleanup Tests")
    class CleanupTests {

        @Test
        @DisplayName("Should delete expired OTPs")
        void cleanupExpiredOtps_DeletesExpired() {
            // When
            otpService.cleanupExpiredOtps();

            // Then
            verify(otpRepository).deleteExpiredOtps(any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("OtpVerification Entity Tests")
    class OtpVerificationEntityTests {

        @Test
        @DisplayName("isExpired returns true when OTP is expired")
        void isExpired_True() {
            OtpVerification otp = OtpVerification.builder()
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .build();

            assertThat(otp.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired returns false when OTP is not expired")
        void isExpired_False() {
            OtpVerification otp = OtpVerification.builder()
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            assertThat(otp.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isValid returns true for valid OTP")
        void isValid_True() {
            OtpVerification otp = OtpVerification.builder()
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .isUsed(false)
                    .attemptCount(0)
                    .build();

            assertThat(otp.isValid()).isTrue();
        }

        @Test
        @DisplayName("isValid returns false when OTP is used")
        void isValid_False_WhenUsed() {
            OtpVerification otp = OtpVerification.builder()
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .isUsed(true)
                    .attemptCount(0)
                    .build();

            assertThat(otp.isValid()).isFalse();
        }

        @Test
        @DisplayName("isValid returns false when max attempts exceeded")
        void isValid_False_WhenMaxAttempts() {
            OtpVerification otp = OtpVerification.builder()
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .isUsed(false)
                    .attemptCount(5)
                    .build();

            assertThat(otp.isValid()).isFalse();
        }
    }

    // Helper methods
    private OtpVerification createValidOtpVerification() {
        return OtpVerification.builder()
                .id(1L)
                .phoneNumber(VALID_PHONE)
                .otpHash(ENCODED_OTP)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .attemptCount(0)
                .build();
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setFullName("Test User");
        user.setPhoneNumber(VALID_PHONE);
        user.setRole(Role.PARENT);
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Parent createTestParent() {
        Parent parent = new Parent();
        parent.setId(1L);
        parent.setEmail("919876543210@phone.badminton-academy.local");
        parent.setFirstName("User");
        parent.setLastName("3210");
        parent.setFullName("User 3210");
        parent.setPhoneNumber(VALID_PHONE);
        parent.setRole(Role.PARENT);
        parent.setIsActive(true);
        parent.setIsEmailVerified(false);
        parent.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return parent;
    }
}
