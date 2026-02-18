package com.badminton.academy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio")
public class TwilioSmsService implements SmsService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()
                || fromNumber == null || fromNumber.isBlank()) {
            log.error("Twilio SMS configuration is missing. Cannot send SMS to {}", phoneNumber);
            return false;
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(accountSid, authToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", phoneNumber);
        body.add("From", fromNumber);
        body.add("Body", message);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Twilio SMS sent successfully to {}", maskPhoneNumber(phoneNumber));
                return true;
            }
            log.error("Twilio SMS failed for {} with status {}", maskPhoneNumber(phoneNumber), response.getStatusCode());
            return false;
        } catch (Exception ex) {
            log.error("Twilio SMS exception for {}: {}", maskPhoneNumber(phoneNumber), ex.getMessage());
            return false;
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "****";
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}
