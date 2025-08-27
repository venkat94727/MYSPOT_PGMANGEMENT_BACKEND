package com.myspot.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxOtpAttempts;

    public String generateOtp() {
        int otp = secureRandom.nextInt(900000) + 100000; // 6-digit OTP
        String otpString = String.valueOf(otp);
        log.debug("Generated OTP: {}", otpString);
        return otpString;
    }

    public boolean verifyOtp(String storedOtp, String providedOtp) {
        if (storedOtp == null || providedOtp == null) {
            return false;
        }

        boolean isValid = storedOtp.equals(providedOtp.trim());
        log.debug("OTP verification result: {}", isValid);
        return isValid;
    }

    public String generateResetToken() {
        String token = UUID.randomUUID().toString();
        log.debug("Generated reset token: {}", token);
        return token;
    }

    public int getOtpExpirationMinutes() {
        return otpExpirationMinutes;
    }

    public int getMaxOtpAttempts() {
        return maxOtpAttempts;
    }
}