package com.sheandsoul.v1update.entities;

import java.time.LocalDate;
import java.util.Map;

import org.hibernate.annotations.Type;

import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Getter
@Setter
@Table(name = "breast_cancer_exam_logs")
public class BreastCancerExamLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;
    
    private LocalDate examDate;

    private int reAccessDate;
    
    private LocalDate createdAt;
    @Type(JsonType.class)
    @Column(name = "symptoms", columnDefinition = "jsonb")
    private Map<SymptomLocation, SymptomSide> symptoms;

}
