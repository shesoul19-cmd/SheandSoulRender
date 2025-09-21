package com.sheandsoul.v1update.services;

import java.time.LocalDate;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.dto.PCOSAssesmentRequest;
import com.sheandsoul.v1update.entities.PCOSAssesment;
import com.sheandsoul.v1update.entities.PcosRiskLevel;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.repository.PCOSAssessmentRepository;

import jakarta.transaction.Transactional;

@Service
public class PcosService {

    private final PCOSAssessmentRepository pcosAssessmentRepository;
    private final AppService appService;

    public PcosService(PCOSAssessmentRepository pcosAssessmentRepository, AppService appService) {
        this.pcosAssessmentRepository = pcosAssessmentRepository;
        this.appService = appService;
    }

    @Transactional
    public PCOSAssesment assessAndSave(Long userId, PCOSAssesmentRequest request) {
        Profile userProfile = appService.findProfileByUserId(userId);

        int criteriaMet = 0;
        int supportingSymptomsCount = 0;

        // --- Evaluate the 3 Core Clinical Criteria ---

        // Criterion 1: Irregular Menstrual Cycles
        // (Defined as cycles > 35 days OR fewer than 8 periods a year)
        if (request.cycleLengthDays() > 35 || request.missedPeriodsInLastYear() >= 4) {
            criteriaMet++;
        }

        // Criterion 2: Clinical Signs of High Androgens
        // (Met if ANY of these key symptoms are present)
        if (request.hasSevereAcne() || request.hasExcessHairGrowth() || request.hasThinningHair()) {
            criteriaMet++;
        }

        // Criterion 3: Polycystic Ovaries Confirmed by a Doctor
        if (request.hasOvarianCystsConfirmedByUltrasound()) {
            criteriaMet++;
        }
        
        // --- Count Supporting Symptoms for Moderate Risk Assessment ---
        if (request.hasWeightGainOrObesity()) supportingSymptomsCount++;
        if (request.hasDarkSkinPatches()) supportingSymptomsCount++;
        if (request.hasFamilyHistoryOfPCOS()) supportingSymptomsCount++;
        if (request.experiencesHighStress()) supportingSymptomsCount++;

        // --- Determine Final Risk Level ---
        PcosRiskLevel riskLevel;
        if (criteriaMet >= 2) {
            // Meets the clinical threshold for high likelihood of PCOS
            riskLevel = PcosRiskLevel.HIGH;
        } else if (criteriaMet == 1 && supportingSymptomsCount >= 2) {
            // Has one major criterion and multiple supporting symptoms
            riskLevel = PcosRiskLevel.MODERATE;
        } else if (criteriaMet == 1) {
            // Has one major criterion, which is significant
            riskLevel = PcosRiskLevel.MODERATE;
        } else {
            // Does not meet any major criteria
            riskLevel = PcosRiskLevel.LOW;
        }

        // Save the final assessment
        PCOSAssesment assessment = new PCOSAssesment();
        assessment.setProfile(userProfile);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setRiskLevel(riskLevel);

        // IMPORTANT: You should modify the `PCOSAssesment` entity to store the new
        // detailed answers from the request, instead of the old `Map`.
        // This will give you much better data for reports. For now, we save an empty map.
        assessment.setSymptoms(new HashMap<>());

        return pcosAssessmentRepository.save(assessment);
    }
}