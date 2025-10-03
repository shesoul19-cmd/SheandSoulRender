package com.sheandsoul.v1update.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for a new, clinically-aligned 10-question PCOS assessment.
 */
public record PCOSAssesmentRequest(
    // --- Questions for Criterion 1: Irregular Menstrual Cycles ---
    @NotNull Integer cycleLengthDays, // e.g., 35
    @NotNull Integer missedPeriodsInLastYear, // e.g., 4

    // --- Questions for Criterion 2: Signs of High Androgens ---
    @NotNull Boolean hasSevereAcne, // Yes/No
    @NotNull Boolean hasExcessHairGrowth, // Yes/No
    @NotNull Boolean hasThinningHair, // Yes/No

    // --- Question for Criterion 3: Polycystic Ovaries ---
    @NotNull Boolean hasOvarianCystsConfirmedByUltrasound, // Yes/No

    // --- Questions for Supporting Symptoms ---
    @NotNull Boolean hasWeightGainOrObesity, // Yes/No
    @NotNull Boolean hasDarkSkinPatches, // Yes/No
    @NotNull Boolean hasFamilyHistoryOfPCOS, // Yes/No
    @NotNull Boolean experiencesHighStress, // Yes/No

    @NotNull Boolean hasMoodSwings,
    @NotNull Boolean hasSleepDisturbances,
    @NotNull Boolean experiencesFatigue,
    @NotNull Boolean hasStrongCravings,
    @NotNull Boolean hasInsulinResistance,
    @NotNull Boolean hasPelvicPain,
    @NotNull Boolean hasFrequentHeadaches,
    @NotNull Boolean hasDifficultyConceiving,
    @NotNull Boolean hasHighBloodPressure,
    @NotNull Boolean hasSkinTags
) {
}