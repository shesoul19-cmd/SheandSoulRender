package com.sheandsoul.v1update.dto;
import java.time.LocalDate;

import com.sheandsoul.v1update.entities.PCOSAssesment;
import com.sheandsoul.v1update.entities.PcosRiskLevel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PcosAssessmentDetailsDto {

    private PcosRiskLevel riskLevel;
    private LocalDate assessmentDate;
    private Integer cycleLengthDays;
    private Integer missedPeriodsInLastYear;
    private Boolean hasSevereAcne;
    private Boolean hasExcessHairGrowth;
    private Boolean hasThinningHair;
    private Boolean hasOvarianCystsConfirmedByUltrasound;
    private Boolean hasWeightGainOrObesity;
    private Boolean hasDarkSkinPatches;
    private Boolean hasFamilyHistoryOfPCOS;
    private Boolean experiencesHighStress;

    // Factory method to easily convert from an entity
    public static PcosAssessmentDetailsDto fromEntity(PCOSAssesment entity) {
        return new PcosAssessmentDetailsDto(
                entity.getRiskLevel(),
                entity.getAssessmentDate(),
                entity.getCycleLengthDays(),
                entity.getMissedPeriodsInLastYear(),
                entity.getHasSevereAcne(),
                entity.getHasExcessHairGrowth(),
                entity.getHasThinningHair(),
                entity.getHasOvarianCystsConfirmedByUltrasound(),
                entity.getHasWeightGainOrObesity(),
                entity.getHasDarkSkinPatches(),
                entity.getHasFamilyHistoryOfPCOS(),
                entity.getExperiencesHighStress()
        );
    }
}