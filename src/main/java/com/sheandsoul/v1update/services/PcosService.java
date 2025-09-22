package com.sheandsoul.v1update.services;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.dto.PCOSAssesmentRequest;
import com.sheandsoul.v1update.dto.PcosAssessmentDetailsDto;
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

    // --- Step 1: Evaluate the 3 Core Clinical Criteria ---

    // Criterion 1: Irregular Menstrual Cycles
    if (request.cycleLengthDays() > 35 || request.missedPeriodsInLastYear() >= 4) {
        criteriaMet++;
    }

    // Criterion 2: Clinical Signs of High Androgens
    if (request.hasSevereAcne() || request.hasExcessHairGrowth() || request.hasThinningHair()) {
        criteriaMet++;
    }

    // Criterion 3: Polycystic Ovaries Confirmed by a Doctor
    if (request.hasOvarianCystsConfirmedByUltrasound()) {
        criteriaMet++;
    }

    // --- Step 2: Count Supporting Symptoms for a more nuanced result ---
    if (request.hasWeightGainOrObesity()) supportingSymptomsCount++;
    if (request.hasDarkSkinPatches()) supportingSymptomsCount++;
    if (request.hasFamilyHistoryOfPCOS()) supportingSymptomsCount++;
    if (request.experiencesHighStress()) supportingSymptomsCount++;

    // --- Step 3: Determine Final Risk Level ---
    PcosRiskLevel riskLevel; // Declare the variable
    if (criteriaMet >= 2) {
        riskLevel = PcosRiskLevel.HIGH;
    } else if (criteriaMet == 1 && supportingSymptomsCount >= 2) {
        riskLevel = PcosRiskLevel.MODERATE;
    } else if (criteriaMet == 1) {
        riskLevel = PcosRiskLevel.MODERATE;
    } else {
        riskLevel = PcosRiskLevel.LOW;
    }

    // --- Step 4: Populate and Save the Entity ---
    PCOSAssesment assessment = pcosAssessmentRepository
            .findTopByProfileIdOrderByAssessmentDateDesc(userProfile.getId())
            .orElseGet(PCOSAssesment::new); // If it doesn't exist, create a new one.

    // Set or update all fields
    assessment.setProfile(userProfile);
    assessment.setAssessmentDate(LocalDate.now()); // Always update to the latest date
    assessment.setRiskLevel(riskLevel);

    assessment.setCycleLengthDays(request.cycleLengthDays());
    assessment.setMissedPeriodsInLastYear(request.missedPeriodsInLastYear());
    assessment.setHasSevereAcne(request.hasSevereAcne());
    assessment.setHasExcessHairGrowth(request.hasExcessHairGrowth());
    assessment.setHasThinningHair(request.hasThinningHair());
    assessment.setHasOvarianCystsConfirmedByUltrasound(request.hasOvarianCystsConfirmedByUltrasound());
    assessment.setHasWeightGainOrObesity(request.hasWeightGainOrObesity());
    assessment.setHasDarkSkinPatches(request.hasDarkSkinPatches());
    assessment.setHasFamilyHistoryOfPCOS(request.hasFamilyHistoryOfPCOS());
    assessment.setExperiencesHighStress(request.experiencesHighStress());

    // JPA's save method will automatically UPDATE if the entity already exists,
    // or INSERT if it's new.
    return pcosAssessmentRepository.save(assessment);
}
@Transactional
public boolean hasCompletedAssessment(Long userId) {
    // Find the user's profile first
    Profile userProfile = appService.findProfileByUserId(userId);
    // Check if any assessment exists for that profile
    return pcosAssessmentRepository.findTopByProfileIdOrderByAssessmentDateDesc(userProfile.getId()).isPresent();
}
 @Transactional
    public Optional<PcosAssessmentDetailsDto> getLatestAssessmentDetails(Long userId) {
        Profile userProfile = appService.findProfileByUserId(userId);
        return pcosAssessmentRepository.findTopByProfileIdOrderByAssessmentDateDesc(userProfile.getId())
                .map(PcosAssessmentDetailsDto::fromEntity);
    }

}