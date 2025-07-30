package com.sheandsoul.v1update.entities;

import java.time.LocalDate;
import java.util.Map;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Table(name = "profiles")
@Getter
@Setter
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    private Integer age;
    private Double height;
    private Double weight;
    @Column(unique = true)
    private String referralCode;

    private String referredCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    @JsonManagedReference
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private UserServiceType preferredServiceType;

    private Integer periodLength;
    private Integer cycleLength;

    private LocalDate lastPeriodStartDate;
    private LocalDate lastPeriodEndDate;

    @Column(columnDefinition = "TEXT")
    private String deviceToken;
    private String languageCode;
    public enum UserType {
        USER, PARTNER
    }
    @Type(JsonType.class)
    @Column(name = "risk_assessment_mcq_data", columnDefinition = "jsonb")
    private Map<String, String> riskAssessmentMcqData;

    @Column(name = "breast_cancer_risk_level")
    private String breastCancerRiskLevel;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> medicalSummary;
}
