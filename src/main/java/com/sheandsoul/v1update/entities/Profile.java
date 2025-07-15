package com.sheandsoul.v1update.entities;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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

@Entity
@Data
@Table(name = "profiles")
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

    @Column(name = "menstrual_service")
    private boolean enableMenstrualService;

    @Column(name = "breast_cancer_service")
    private boolean enableBreastCancerService;

    private Integer periodLength;
    private Integer cycleLength;

    private LocalDate lastPeriodStartDate;
    private LocalDate lastPeriodEndDate;

    private String languageCode;



    public enum UserType {
        USER, PARTNER
    }
}
