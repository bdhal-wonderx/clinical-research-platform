package com.wonderx.rwe.service;

import com.wonderx.rwe.config.OtpProperties;
import com.wonderx.rwe.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final String OTP_KEY_PREFIX = "otp:";
    private final StringRedisTemplate redisTemplate;
    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, String> inMemoryOtpStore = new ConcurrentHashMap<>();

    public void sendOtp(String mobileNumber) {
        String otp = generateOtp();
        String key = OTP_KEY_PREFIX + mobileNumber;

        try {
            redisTemplate.opsForValue().set(key, otp, otpProperties.getExpirationSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis unavailable, using in-memory OTP store for dev");
            inMemoryOtpStore.put(mobileNumber, otp);
        }

        log.info("OTP sent to {} (dev mode - OTP: {})", maskMobile(mobileNumber), otp);
    }

    public boolean verifyOtp(String mobileNumber, String otp) {
        String key = OTP_KEY_PREFIX + mobileNumber;
        String storedOtp = null;

        try {
            storedOtp = redisTemplate.opsForValue().get(key);
            if (storedOtp != null) {
                if (!storedOtp.equals(otp)) {
                    throw new BusinessException("Invalid OTP");
                }
                redisTemplate.delete(key);
                return true;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable during OTP verification, checking in-memory store");
        }

        storedOtp = inMemoryOtpStore.get(mobileNumber);
        if (storedOtp == null) {
            throw new BusinessException("OTP expired or not found. Please request a new OTP.");
        }

        if (!storedOtp.equals(otp)) {
            throw new BusinessException("Invalid OTP");
        }

        inMemoryOtpStore.remove(mobileNumber);
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
}
