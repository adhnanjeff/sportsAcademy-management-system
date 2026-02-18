package com.badminton.academy.service;

/**
 * SMS Service interface for sending SMS messages.
 * Implement this interface with your preferred SMS provider (Twilio, AWS SNS, etc.)
 */
public interface SmsService {

    /**
     * Send an SMS message to the specified phone number.
     *
     * @param phoneNumber The destination phone number in international format
     * @param message The message content to send
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendSms(String phoneNumber, String message);

    /**
     * Send an OTP code to the specified phone number.
     *
     * @param phoneNumber The destination phone number in international format
     * @param otp The OTP code to send
     * @return true if the OTP was sent successfully, false otherwise
     */
    default boolean sendOtp(String phoneNumber, String otp) {
        String message = String.format("Your Badminton Academy verification code is: %s. Valid for 5 minutes. Do not share this code.", otp);
        return sendSms(phoneNumber, message);
    }
}
