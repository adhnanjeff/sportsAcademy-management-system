package com.badminton.academy.service;

import com.badminton.academy.dto.request.OtpRequestDto;
import com.badminton.academy.dto.request.OtpVerifyDto;
import com.badminton.academy.dto.request.SignupOtpRequest;
import com.badminton.academy.dto.response.AuthResponse;
import com.badminton.academy.dto.response.OtpResponse;
import com.badminton.academy.dto.response.UserResponse;
import com.badminton.academy.model.OtpVerification;
import com.badminton.academy.model.Parent;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.OtpChannel;
import com.badminton.academy.model.enums.Role;
import com.badminton.academy.repository.OtpVerificationRepository;
import com.badminton.academy.repository.ParentRepository;
import com.badminton.academy.repository.UserRepository;
import com.badminton.academy.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.max-requests-per-hour:5}")
    private int maxRequestsPerHour;

    @Value("${otp.max-verify-attempts:5}")
    private int maxVerifyAttempts;

    private enum OtpPurpose {
        LOGIN,
        SIGNUP,
        PASSWORD_RESET
    }

    @Transactional
    public OtpResponse requestOtp(OtpRequestDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        return requestOtpInternal(phoneNumber, OtpChannel.PHONE, OtpPurpose.LOGIN);
    }

    @Transactional
    public OtpResponse requestSignupOtp(SignupOtpRequest request) {
        String identifier = resolveIdentifier(request.getChannel(), request.getEmail(), request.getPhoneNumber());
        return requestOtpInternal(identifier, request.getChannel(), OtpPurpose.SIGNUP);
    }

    @Transactional
    public OtpResponse requestPasswordResetOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        return requestOtpInternal(normalizedEmail, OtpChannel.EMAIL, OtpPurpose.PASSWORD_RESET);
    }

    @Transactional
    public void verifyPasswordResetOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        verifyOtpInternal(buildOtpKey(normalizedEmail, OtpChannel.EMAIL, OtpPurpose.PASSWORD_RESET), otp, false);
    }

    @Transactional
    public void consumePasswordResetOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        verifyOtpInternal(buildOtpKey(normalizedEmail, OtpChannel.EMAIL, OtpPurpose.PASSWORD_RESET), otp, true);
    }

    @Transactional
    public void consumeSignupOtp(OtpChannel channel, String email, String phoneNumber, String otp) {
        String identifier = resolveIdentifier(channel, email, phoneNumber);
        verifyOtpInternal(buildOtpKey(identifier, channel, OtpPurpose.SIGNUP), otp, true);
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        verifyOtpInternal(buildOtpKey(phoneNumber, OtpChannel.PHONE, OtpPurpose.LOGIN), request.getOtp(), true);

        User user = findOrCreateUserByPhone(phoneNumber);

        String accessToken = jwtUtils.generateTokenFromUsername(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        log.info("OTP verified successfully for phone number: {}", maskPhoneNumber(phoneNumber));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(mapToUserResponse(user))
                .build();
    }

    private OtpResponse requestOtpInternal(String identifier, OtpChannel channel, OtpPurpose purpose) {
        String otpKey = buildOtpKey(identifier, channel, purpose);

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = otpRepository.countRecentOtpRequests(otpKey, oneHourAgo);
        if (recentRequests >= maxRequestsPerHour) {
            log.warn("Rate limit exceeded for OTP key: {}", maskKeyForLog(otpKey));
            return OtpResponse.rateLimited(3600);
        }

        otpRepository.invalidateAllOtpsForPhoneNumber(otpKey);

        String otp = generateOtp();
        OtpVerification otpVerification = OtpVerification.builder()
                .phoneNumber(otpKey)
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .isUsed(false)
                .attemptCount(0)
                .build();
        otpRepository.save(otpVerification);

        boolean sent = sendOtp(channel, identifier, otp, purpose);
        if (!sent) {
            log.error("Failed to send OTP to destination: {}", maskDestination(channel, identifier));
            return OtpResponse.error("Failed to send OTP. Please try again.");
        }

        log.info("OTP sent successfully to destination: {}", maskDestination(channel, identifier));
        return OtpResponse.builder()
                .success(true)
                .message("OTP sent successfully")
                .phoneNumber(maskDestination(channel, identifier))
                .expiresInSeconds(otpExpiryMinutes * 60)
                .build();
    }

    private boolean sendOtp(OtpChannel channel, String destination, String otp, OtpPurpose purpose) {
        if (channel == OtpChannel.PHONE) {
            return smsService.sendOtp(destination, otp);
        }

        String purposeLabel = switch (purpose) {
            case LOGIN -> "login";
            case SIGNUP -> "signup";
            case PASSWORD_RESET -> "password reset";
        };
        return emailService.sendOtp(destination, otp, purposeLabel);
    }

    private void verifyOtpInternal(String otpKey, String otp, boolean consume) {
        Optional<OtpVerification> otpOpt = otpRepository.findLatestValidOtp(otpKey, LocalDateTime.now());
        if (otpOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please request a new one.");
        }

        OtpVerification otpVerification = otpOpt.get();
        if (otpVerification.getAttemptCount() >= maxVerifyAttempts) {
            otpVerification.setIsUsed(true);
            otpRepository.save(otpVerification);
            throw new IllegalArgumentException("Too many failed attempts. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(otp, otpVerification.getOtpHash())) {
            otpVerification.setAttemptCount(otpVerification.getAttemptCount() + 1);
            otpRepository.save(otpVerification);

            int remainingAttempts = maxVerifyAttempts - otpVerification.getAttemptCount();
            throw new IllegalArgumentException(String.format("Invalid OTP. %d attempts remaining.", remainingAttempts));
        }

        if (consume) {
            otpVerification.setIsUsed(true);
            otpRepository.save(otpVerification);
        }
    }

    private User findOrCreateUserByPhone(String phoneNumber) {
        Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!user.getIsActive()) {
                throw new IllegalArgumentException("Account is deactivated. Please contact support.");
            }
            return user;
        }

        log.info("Creating new user for phone number: {}", maskPhoneNumber(phoneNumber));

        Parent newParent = new Parent();
        newParent.setPhoneNumber(phoneNumber);
        newParent.setEmail(phoneNumber.replaceAll("\\+", "") + "@phone.badminton-academy.local");
        newParent.setPassword(passwordEncoder.encode(generateOtp() + generateOtp()));
        newParent.setFirstName("User");
        newParent.setLastName(phoneNumber.substring(Math.max(0, phoneNumber.length() - 4)));
        newParent.setFullName("User " + phoneNumber.substring(Math.max(0, phoneNumber.length() - 4)));
        newParent.setDateOfBirth(LocalDate.of(1990, 1, 1));
        newParent.setRole(Role.PARENT);
        newParent.setIsActive(true);
        newParent.setIsEmailVerified(false);

        return parentRepository.save(newParent);
    }

    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    private String buildOtpKey(String identifier, OtpChannel channel, OtpPurpose purpose) {
        return purpose.name() + ":" + channel.name() + ":" + identifier;
    }

    private String resolveIdentifier(OtpChannel channel, String email, String phoneNumber) {
        if (channel == OtpChannel.EMAIL) {
            return normalizeEmail(email);
        }
        if (channel == OtpChannel.PHONE) {
            return normalizePhoneNumber(phoneNumber);
        }
        throw new IllegalArgumentException("Invalid OTP channel");
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        return phoneNumber.replaceAll("[\\s\\-()]", "").trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase();
    }

    private String maskDestination(OtpChannel channel, String destination) {
        if (channel == OtpChannel.PHONE) {
            return maskPhoneNumber(destination);
        }
        return maskEmail(destination);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        if (local.length() <= 2) {
            return "**@" + parts[1];
        }
        return local.substring(0, 2) + "****@" + parts[1];
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "****";
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }

    private String maskKeyForLog(String key) {
        if (key == null) {
            return "****";
        }
        return key.length() <= 10 ? "********" : key.substring(0, 10) + "********";
    }

    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleaned up expired OTPs");
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
