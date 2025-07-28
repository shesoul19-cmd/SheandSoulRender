package com.sheandsoul.v1update.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sheandsoul.v1update.entities.Otp;
import com.sheandsoul.v1update.repository.OtpRepository;

@Service
public class OtpGenerationService {

    private static final int OTP_LENGTH = 6;

    private final OtpRepository otpRepository;

    public OtpGenerationService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public void storeOtp(String email, String otp) {
        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtpCode(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpEntity.setUsed(false);
        
        otpRepository.save(otpEntity);
    }

    public String getLatestOtp(String email) {
        LocalDateTime now = LocalDateTime.now();
        var validOtps = otpRepository.findValidOtpsByEmail(email, now);
        
        if (validOtps.isEmpty()) {
            return null;
        }
        
        return validOtps.get(0).getOtpCode();
    }

    public boolean isOtpValid(String email, String otpCode) {
        LocalDateTime now = LocalDateTime.now();
        return otpRepository.findValidOtpByEmailAndCode(email, otpCode, now).isPresent();
    }

    @Transactional
    public void markOtpAsUsed(String email) {
        otpRepository.markAllOtpsAsUsed(email);
    }

    @Transactional
    public void clearOtps(String email) {
        otpRepository.deleteAllOtpsByEmail(email);
    }

    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}
