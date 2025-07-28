package com.sheandsoul.v1update.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.PCOSAssesment;

public interface PCOSAssessmentRepository extends JpaRepository<PCOSAssesment, Long> {

    Optional<PCOSAssesment>findTopByProfileIdOrderByAssessmentDateDesc(Long profileId);
}
