package com.badminton.academy.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OTP DTO Validation Tests")
class OtpDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("OtpRequestDto Validation Tests")
    class OtpRequestDtoTests {

        @Test
        @DisplayName("Valid international phone number should pass validation")
        void validPhoneNumber_NoViolations() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber("+919876543210")
                    .build();

            // When
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "+1234567890",
                "+919876543210",
                "+447911123456",
                "+33612345678",
                "+8613812345678",
                "+96599999999"
        })
        @DisplayName("Various valid international formats should pass")
        void validInternationalFormats(String phoneNumber) {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(phoneNumber)
                    .build();

            // When
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Null phone number should fail validation")
        void nullPhoneNumber_HasViolation() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(null)
                    .build();

            // When
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Phone number is required");
        }

        @Test
        @DisplayName("Blank phone number should fail validation")
        void blankPhoneNumber_HasViolation() {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber("")
                    .build();

            // When
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "9876543210",     // Missing + prefix
                "+0123456789",    // Starts with 0 after +
                "+12345",         // Too short
                "+12345678901234567890", // Too long
                "abc123",         // Contains letters
                "+91-98765-43210" // Contains dashes
        })
        @DisplayName("Invalid phone formats should fail validation")
        void invalidPhoneFormats_HasViolation(String phoneNumber) {
            // Given
            OtpRequestDto request = OtpRequestDto.builder()
                    .phoneNumber(phoneNumber)
                    .build();

            // When
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("OtpVerifyDto Validation Tests")
    class OtpVerifyDtoTests {

        @Test
        @DisplayName("Valid phone and OTP should pass validation")
        void validRequest_NoViolations() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber("+919876543210")
                    .otp("123456")
                    .build();

            // When
            Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Null phone number should fail validation")
        void nullPhoneNumber_HasViolation() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber(null)
                    .otp("123456")
                    .build();

            // When
            Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("Null OTP should fail validation")
        void nullOtp_HasViolation() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber("+919876543210")
                    .otp(null)
                    .build();

            // When
            Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("Blank OTP should fail validation")
        void blankOtp_HasViolation() {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber("+919876543210")
                    .otp("")
                    .build();

            // When
            Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "12345",      // Too short (5 digits)
                "1234567",    // Too long (7 digits)
                "12345a",     // Contains letter
                "12 345",     // Contains space
                "1234.5",     // Contains dot
                "-12345"      // Negative
        })
        @DisplayName("Invalid OTP formats should fail validation")
        void invalidOtpFormats_HasViolation(String otp) {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber("+919876543210")
                    .otp(otp)
                    .build();

            // When
            Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"000000", "123456", "999999", "100000"})
        @DisplayName("Valid 6-digit OTP codes should pass validation")
        void validOtpCodes(String otp) {
            // Given
            OtpVerifyDto request = OtpVerifyDto.builder()
                    .phoneNumber("+919876543210")
                    .otp(otp)
                    .build();

            // When
            Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }
}
