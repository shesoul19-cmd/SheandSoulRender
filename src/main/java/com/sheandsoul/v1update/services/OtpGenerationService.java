package com.sheandsoul.v1update.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OtpGenerationService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALID_DURATION_MINUTES = 10;

    // In-memory cache for OTPs. For production, use a distributed cache like Redis.
    private final ConcurrentHashMap<String, String> otpCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> otpExpiryCache = new ConcurrentHashMap<>();

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void storeOtp(String email, String otp) {
        otpCache.put(email, otp);
        otpExpiryCache.put(email, LocalDateTime.now().plusMinutes(OTP_VALID_DURATION_MINUTES));
    }

    public String getOtp(String email) {
        return otpCache.get(email);
    }

    public boolean isOtpExpired(String email) {
        LocalDateTime expiryTime = otpExpiryCache.get(email);
        return expiryTime == null || expiryTime.isBefore(LocalDateTime.now());
    }

    public void clearOtp(String email) {
        otpCache.remove(email);
        otpExpiryCache.remove(email);
    }

}
