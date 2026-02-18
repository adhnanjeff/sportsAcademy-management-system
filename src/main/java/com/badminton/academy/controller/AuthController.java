package com.badminton.academy.controller;

import com.badminton.academy.dto.request.LoginRequest;
import com.badminton.academy.dto.request.OtpRequestDto;
import com.badminton.academy.dto.request.OtpVerifyDto;
import com.badminton.academy.dto.request.PasswordOtpRequest;
import com.badminton.academy.dto.request.PasswordOtpVerifyRequest;
import com.badminton.academy.dto.request.PasswordResetRequest;
import com.badminton.academy.dto.request.RefreshTokenRequest;
import com.badminton.academy.dto.request.RegisterRequest;
import com.badminton.academy.dto.request.SignupOtpRequest;
import com.badminton.academy.dto.response.AuthResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.dto.response.OtpResponse;
import com.badminton.academy.dto.response.UserResponse;
import com.badminton.academy.model.User;
import com.badminton.academy.service.AuthService;
import com.badminton.academy.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    /**
     * Health check endpoint for backend warm-up
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/signup/otp/request")
    public ResponseEntity<OtpResponse> requestSignupOtp(@Valid @RequestBody SignupOtpRequest request) {
        OtpResponse response = authService.requestSignupOtp(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Request OTP for phone number login
     * POST /api/auth/otp/request
     */
    @PostMapping("/otp/request")
    public ResponseEntity<OtpResponse> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        OtpResponse response = otpService.requestOtp(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Verify OTP and login
     * POST /api/auth/otp/verify
     */
    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        AuthResponse response = otpService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = authService.getCurrentUser();
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logout() {
        // JWT is stateless, so just return success
        // Client should discard the tokens
        return ResponseEntity.ok(MessageResponse.success("Logged out successfully"));
    }

    @PostMapping("/password/otp/request")
    public ResponseEntity<OtpResponse> requestPasswordOtp(@Valid @RequestBody PasswordOtpRequest request) {
        OtpResponse response = authService.requestPasswordResetOtp(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/password/otp/verify")
    public ResponseEntity<MessageResponse> verifyPasswordOtp(@Valid @RequestBody PasswordOtpVerifyRequest request) {
        authService.verifyPasswordResetOtp(request);
        return ResponseEntity.ok(MessageResponse.success("OTP verified successfully"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(MessageResponse.success("Password reset successfully"));
    }
}
