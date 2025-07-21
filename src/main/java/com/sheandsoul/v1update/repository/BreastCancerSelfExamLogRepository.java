package com.sheandsoul.v1update.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.BreastCancerExamLog;

public interface BreastCancerSelfExamLogRepository extends JpaRepository<BreastCancerExamLog, Long> {

    Optional<BreastCancerExamLog>findTopByProfileIdOrderByExamDateDesc(Long profileId);

}
