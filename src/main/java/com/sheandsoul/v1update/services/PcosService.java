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

       // ... existing logic for calculating criteriaMet, supportingSymptomsCount, and riskLevel ...
       int criteriaMet = 0;
       int supportingSymptomsCount = 0;
       if (request.cycleLengthDays() > 35 || request.missedPeriodsInLastYear() >= 4) criteriaMet++;
       if (request.hasSevereAcne() || request.hasExcessHairGrowth() || request.hasThinningHair()) criteriaMet++;
       if (request.hasOvarianCystsConfirmedByUltrasound()) criteriaMet++;
       if (request.hasWeightGainOrObesity()) supportingSymptomsCount++;
       if (request.hasDarkSkinPatches()) supportingSymptomsCount++;
       if (request.hasFamilyHistoryOfPCOS()) supportingSymptomsCount++;
       if (request.experiencesHighStress()) supportingSymptomsCount++;

       PcosRiskLevel riskLevel;
       if (criteriaMet >= 2) {
           riskLevel = PcosRiskLevel.HIGH;
       } else if (criteriaMet == 1 && supportingSymptomsCount >= 2) {
           riskLevel = PcosRiskLevel.MODERATE;
       } else {
           riskLevel = PcosRiskLevel.LOW;
       }
       // ... end of existing logic

       PCOSAssesment assessment = pcosAssessmentRepository
               .findTopByProfileIdOrderByAssessmentDateDesc(userProfile.getId())
               .orElseGet(PCOSAssesment::new);

       assessment.setProfile(userProfile);
       assessment.setAssessmentDate(LocalDate.now());
       assessment.setRiskLevel(riskLevel);

       // --- Map all 20 fields from the request to the entity ---
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
       
       // ✅ ADD MAPPING FOR THE 10 NEW FIELDS
       assessment.setHasMoodSwings(request.hasMoodSwings());
       assessment.setHasSleepDisturbances(request.hasSleepDisturbances());
       assessment.setExperiencesFatigue(request.experiencesFatigue());
       assessment.setHasStrongCravings(request.hasStrongCravings());
       assessment.setHasInsulinResistance(request.hasInsulinResistance());
       assessment.setHasPelvicPain(request.hasPelvicPain());
       assessment.setHasFrequentHeadaches(request.hasFrequentHeadaches());
       assessment.setHasDifficultyConceiving(request.hasDifficultyConceiving());
       assessment.setHasHighBloodPressure(request.hasHighBloodPressure());
       assessment.setHasSkinTags(request.hasSkinTags());

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