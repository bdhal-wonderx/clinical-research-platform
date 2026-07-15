package com.wonderx.rwe.service;

import com.wonderx.rwe.config.OtpProperties;
import com.wonderx.rwe.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public void sendOtp(String mobileNumber) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(otpProperties.getExpirationSeconds());
        otpStore.put(mobileNumber, new OtpEntry(otp, expiresAt));
        log.info("OTP sent to {} (dev mode - OTP: {})", maskMobile(mobileNumber), otp);
    }

    public boolean verifyOtp(String mobileNumber, String otp) {
        OtpEntry entry = otpStore.get(mobileNumber);
        if (entry == null) {
            throw new BusinessException("OTP expired or not found. Please request a new OTP.");
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(mobileNumber);
            throw new BusinessException("OTP expired or not found. Please request a new OTP.");
        }
        if (!entry.otp().equals(otp)) {
            throw new BusinessException("Invalid OTP");
        }
        otpStore.remove(mobileNumber);
        return true;
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, otpProperties.getLength());
        int otp = secureRandom.nextInt(bound / 10, bound);
        return String.valueOf(otp);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return "****";
        }
        return "******" + mobile.substring(mobile.length() - 4);
    }

    private record OtpEntry(String otp, Instant expiresAt) {}
}
