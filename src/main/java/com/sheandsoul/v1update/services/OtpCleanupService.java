package com.sheandsoul.v1update.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OtpCleanupService {

    private final OtpGenerationService otpGenerationService;

    public OtpCleanupService(OtpGenerationService otpGenerationService) {
        this.otpGenerationService = otpGenerationService;
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredOtps() {
        otpGenerationService.cleanupExpiredOtps();
    }
} 