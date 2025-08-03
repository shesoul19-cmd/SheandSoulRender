package com.sheandsoul.v1update.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sheandsoul.v1update.entities.Otp;
import com.sheandsoul.v1update.repository.OtpRepository;

@Service
public class OtpGenerationService {

    private static final int OTP_LENGTH = 6;
    private static final Logger logger = LoggerFactory.getLogger(OtpGenerationService.class);

    private final OtpRepository otpRepository;

    public OtpGenerationService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        String otpStr = String.valueOf(otp);
        logger.info("Generated new OTP."); // Don't log the OTP itself for security
        return otpStr;
    }

    @Transactional
    public void storeOtp(String email, String otp) {
        logger.info("Storing OTP for email: {}", email);
        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtpCode(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpEntity.setUsed(false);
        
        otpRepository.save(otpEntity);
        logger.info("OTP stored successfully for email: {}", email);
    }

    public String getLatestOtp(String email) {
        LocalDateTime now = LocalDateTime.now();
        var validOtps = otpRepository.findValidOtpsByEmail(email, now);
        
        if (validOtps.isEmpty()) {
            logger.warn("No valid OTP found for email: {}", email);
            return null;
        }
        
        logger.info("Retrieved latest valid OTP for email: {}", email);
        return validOtps.get(0).getOtpCode();
    }

    public boolean isOtpValid(String email, String otpCode) {
        LocalDateTime now = LocalDateTime.now();
        boolean isValid = otpRepository.findValidOtpByEmailAndCode(email, otpCode, now).isPresent();
        logger.info("Validating OTP for email: {}. Valid: {}", email, isValid);
        return isValid;
    }

    @Transactional
    public void markOtpAsUsed(String email) {
        logger.info("Marking all OTPs as used for email: {}", email);
        otpRepository.markAllOtpsAsUsed(email);
    }

    @Transactional
    public void clearOtps(String email) {
        logger.info("Clearing all OTPs for email: {}", email);
        otpRepository.deleteAllOtpsByEmail(email);
    }

    @Transactional
    public void cleanupExpiredOtps() {
        logger.info("Cleaning up expired OTPs.");
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}
