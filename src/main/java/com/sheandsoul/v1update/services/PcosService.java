package com.sheandsoul.v1update.services;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.dto.PCOSAssesmentRequest;
import com.sheandsoul.v1update.entities.PCOSAssesment;
import com.sheandsoul.v1update.entities.PCOSSymptoms;
import com.sheandsoul.v1update.entities.PcosRiskLevel;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.SymptomSeverity;
import com.sheandsoul.v1update.repository.PCOSAssessmentRepository;

import jakarta.transaction.Transactional;

@Service
public class PcosService {

    private final PCOSAssessmentRepository pcosAssessmentRepository;
    private final AppService appService; // Your main service to find user profiles

    public PcosService(PCOSAssessmentRepository pcosAssessmentRepository, AppService appService) {
        this.pcosAssessmentRepository = pcosAssessmentRepository;
        this.appService = appService;
    }

    @Transactional
    public PCOSAssesment assessAndSave(Long userId, PCOSAssesmentRequest request) {
        Profile userProfile = appService.findProfileByUserId(userId);

        int score =calculateRiskScore(request.symptoms());
        PcosRiskLevel riskLevel = classifyRisk(score);

        PCOSAssesment assessment = new PCOSAssesment();
        assessment.setProfile(userProfile);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setSymptoms(request.symptoms());
        assessment.setRiskLevel(riskLevel);

        return pcosAssessmentRepository.save(assessment);
    }

    private int calculateRiskScore(Map<PCOSSymptoms, SymptomSeverity> symptoms) {
        return symptoms.values().stream()
            .mapToInt(severity -> switch (severity) {
                case MILD -> 1;
                case MODERATE -> 2;
                case SEVERE -> 3;
                default -> 0; // NONE
            })
            .sum();
    }

    private PcosRiskLevel classifyRisk(int score) {
        if (score >= 15) {
            return PcosRiskLevel.HIGH;
        } else if (score >= 8) {
            return PcosRiskLevel.MODERATE;
        } else {
            return PcosRiskLevel.LOW;
        }
    }

}
