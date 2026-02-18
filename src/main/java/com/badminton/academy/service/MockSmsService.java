package com.badminton.academy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mock SMS Service for development/testing.
 * Logs OTP to console instead of sending actual SMS.
 * 
 * To use a real SMS provider, implement SmsService and replace this bean.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsService implements SmsService {

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        log.info("========================================");
        log.info("MOCK SMS SERVICE - Message sent to: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("========================================");
        return true;
    }

    @Override
    public boolean sendOtp(String phoneNumber, String otp) {
        log.info("========================================");
        log.info("MOCK SMS SERVICE - OTP sent to: {}", phoneNumber);
        log.info("OTP Code: {}", otp);
        log.info("========================================");
        return true;
    }
}
