package com.badminton.academy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:adhnanjeff26@gmail.com}")
    private String fromAddress;

    public boolean sendOtp(String email, String otp, String purposeLabel) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("Badminton Academy OTP Verification");
            message.setText(buildOtpBody(otp, purposeLabel));
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", email, ex.getMessage());
            return false;
        }
    }

    private String buildOtpBody(String otp, String purposeLabel) {
        return "Your Badminton Academy OTP for " + purposeLabel + " is: " + otp + "\n\n"
+                "This code is valid for 5 minutes. Do not share this code with anyone.";
    }
}
