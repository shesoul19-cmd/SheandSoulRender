package com.sheandsoul.v1update.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pcos_assessment") // Corrected table name to match your DB
@Data
public class PCOSAssesment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "pcos_risk_level")
    private PcosRiskLevel riskLevel;

    // --- FIX: Add new fields to store quiz answers ---
    @Column(name = "cycle_length_days")
    private Integer cycleLengthDays;

    @Column(name = "missed_periods_last_year")
    private Integer missedPeriodsInLastYear;

    @Column(name = "has_severe_acne")
    private Boolean hasSevereAcne;

    @Column(name = "has_excess_hair_growth")
    private Boolean hasExcessHairGrowth;

    @Column(name = "has_thinning_hair")
    private Boolean hasThinningHair;

    @Column(name = "has_ovarian_cysts_confirmed")
    private Boolean hasOvarianCystsConfirmedByUltrasound;

    @Column(name = "has_weight_gain")
    private Boolean hasWeightGainOrObesity;

    @Column(name = "has_dark_skin_patches")
    private Boolean hasDarkSkinPatches;

    @Column(name = "has_family_history")
    private Boolean hasFamilyHistoryOfPCOS;

    @Column(name = "has_high_stress")
    private Boolean experiencesHighStress;

     // ✅ ADD THESE 10 NEW COLUMNS FOR QUESTIONS 11-20
    @Column(name = "has_mood_swings")
    private Boolean hasMoodSwings;

    @Column(name = "has_sleep_disturbances")
    private Boolean hasSleepDisturbances;

    @Column(name = "experiences_fatigue")
    private Boolean experiencesFatigue;

    @Column(name = "has_strong_cravings")
    private Boolean hasStrongCravings;

    @Column(name = "has_insulin_resistance")
    private Boolean hasInsulinResistance;

    @Column(name = "has_pelvic_pain")
    private Boolean hasPelvicPain;

    @Column(name = "has_frequent_headaches")
    private Boolean hasFrequentHeadaches;

    @Column(name = "has_difficulty_conceiving")
    private Boolean hasDifficultyConceiving;

    @Column(name = "has_high_blood_pressure")
    private Boolean hasHighBloodPressure;

    @Column(name = "has_skin_tags")
    private Boolean hasSkinTags;
}