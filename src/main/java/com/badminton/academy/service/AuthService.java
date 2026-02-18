package com.badminton.academy.service;

import com.badminton.academy.dto.request.LoginRequest;
import com.badminton.academy.dto.request.PasswordOtpRequest;
import com.badminton.academy.dto.request.PasswordOtpVerifyRequest;
import com.badminton.academy.dto.request.PasswordResetRequest;
import com.badminton.academy.dto.request.RefreshTokenRequest;
import com.badminton.academy.dto.request.RegisterRequest;
import com.badminton.academy.dto.request.SignupOtpRequest;
import com.badminton.academy.dto.response.AuthResponse;
import com.badminton.academy.dto.response.OtpResponse;
import com.badminton.academy.dto.response.UserResponse;
import com.badminton.academy.exception.DuplicateResourceException;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Parent;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.OtpChannel;
import com.badminton.academy.model.enums.Role;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.ParentRepository;
import com.badminton.academy.repository.UserRepository;
import com.badminton.academy.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CoachRepository coachRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        boolean privilegedRequest = isPrivilegedRegistrationRequest();
        Role targetRole = resolveTargetRole(request, privilegedRequest);
        String normalizedEmail = resolveNormalizedEmail(request, targetRole);
        String normalizedPhone = normalizeOptional(request.getPhoneNumber());
        String normalizedNationalId = normalizeOptional(request.getNationalIdNumber());
        String rawPassword = resolveRawPassword(request, targetRole);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already registered: " + normalizedEmail);
        }

        if (normalizedNationalId != null && userRepository.existsByNationalIdNumber(normalizedNationalId)) {
            throw new DuplicateResourceException("User already exists with national ID: " + normalizedNationalId);
        }

        // Check phone number uniqueness
        if (normalizedPhone != null && userRepository.existsByPhoneNumber(normalizedPhone)) {
            throw new DuplicateResourceException("Phone number already registered: " + normalizedPhone);
        }

        if (!privilegedRequest) {
            if (request.getOtp() == null || request.getOtp().isBlank()) {
                throw new IllegalArgumentException("OTP verification is required before signup");
            }
            OtpChannel otpChannel = request.getOtpChannel() != null ? request.getOtpChannel() : OtpChannel.EMAIL;
            otpService.consumeSignupOtp(otpChannel, normalizedEmail, normalizedPhone, request.getOtp());
        }

        User user;
        switch (targetRole) {
            case COACH -> {
                Coach coach = new Coach();
                setCommonUserFields(coach, request, targetRole, normalizedEmail, rawPassword, normalizedPhone, normalizedNationalId);
                coach.setSpecialization(request.getSpecialization());
                coach.setYearsOfExperience(request.getYearsOfExperience());
                coach.setBio(request.getBio());
                coach.setCertifications(request.getCertifications());
                user = coachRepository.save(coach);
            }
            case PARENT -> {
                Parent parent = new Parent();
                setCommonUserFields(parent, request, targetRole, normalizedEmail, rawPassword, normalizedPhone, normalizedNationalId);
                user = parentRepository.save(parent);
            }
            case ADMIN -> {
                user = new User();
                setCommonUserFields(user, request, targetRole, normalizedEmail, rawPassword, normalizedPhone, normalizedNationalId);
                user = userRepository.save(user);
            }
            default -> throw new IllegalArgumentException("Invalid role: " + targetRole);
        }

        String accessToken = jwtUtils.generateTokenFromUsername(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        log.info("User registered successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(mapToUserResponse(user))
                .build();
    }

    public OtpResponse requestSignupOtp(SignupOtpRequest request) {
        return otpService.requestSignupOtp(request);
    }

    public OtpResponse requestPasswordResetOtp(PasswordOtpRequest request) {
        userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with this email"));
        return otpService.requestPasswordResetOtp(request.getEmail());
    }

    public void verifyPasswordResetOtp(PasswordOtpVerifyRequest request) {
        otpService.verifyPasswordResetOtp(request.getEmail(), request.getOtp());
    }

    @Transactional
    public void resetPasswordWithOtp(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with this email"));

        otpService.consumePasswordResetOtp(request.getEmail(), request.getOtp());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset successful for user: {}", user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String email = jwtUtils.getUsernameFromJwtToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtUtils.generateTokenFromUsername(email);
        String newRefreshToken = jwtUtils.generateRefreshToken(email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(mapToUserResponse(user))
                .build();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void setCommonUserFields(
            User user,
            RegisterRequest request,
            Role role,
            String normalizedEmail,
            String rawPassword,
            String normalizedPhone,
            String normalizedNationalId
    ) {
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setFullName(request.getFirstName() + " " + request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setNationalIdNumber(normalizedNationalId);
        user.setPhoneNumber(normalizedPhone);
        user.setRole(role);
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setPhotoUrl(request.getPhotoUrl());
        user.setIsActive(true);
        user.setIsEmailVerified(true);
    }

    private boolean isPrivilegedRegistrationRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_COACH".equals(authority.getAuthority()));
    }

    private Role resolveTargetRole(RegisterRequest request, boolean privilegedRequest) {
        Role requestedRole = request.getRole();
        if (!privilegedRequest) {
            return Role.PARENT;
        }

        if (requestedRole == null) {
            throw new IllegalArgumentException("Role is required for admin/coach-managed user creation");
        }

        if (requestedRole == Role.ADMIN) {
            throw new IllegalArgumentException("Creating admin users via this endpoint is not allowed");
        }

        if (requestedRole == Role.COACH && !isCurrentUserAdmin()) {
            throw new IllegalArgumentException("Only admins can create coaches");
        }

        if (requestedRole == Role.PARENT && !isCurrentUserAdmin()) {
            throw new IllegalArgumentException("Only admins can create parent users from dashboard");
        }

        return requestedRole;
    }

    private String resolveNormalizedEmail(RegisterRequest request, Role targetRole) {
        String normalizedEmail = normalizeOptional(request.getEmail());

        if (normalizedEmail != null) {
            return normalizedEmail.toLowerCase(Locale.ROOT);
        }

        throw new IllegalArgumentException("Email is required");
    }

    private String resolveRawPassword(RegisterRequest request, Role targetRole) {
        String rawPassword = normalizeOptional(request.getPassword());
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password is required");
        }

        if (rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        return rawPassword;
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .nationalIdNumber(user.getNationalIdNumber())
                .dateOfBirth(user.getDateOfBirth())
                .age(calculateAge(user.getDateOfBirth()))
                .phoneNumber(user.getPhoneNumber())
                .photoUrl(user.getPhotoUrl())
                .address(user.getAddress())
                .city(user.getCity())
                .state(user.getState())
                .country(user.getCountry())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
