package com.sheandsoul.v1update.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.MenstrualCycleLog;

public interface MenstrualCycleLogRepository extends JpaRepository<MenstrualCycleLog, Long> {
  List<MenstrualCycleLog> findByProfileIdOrderByLogDateDesc(Long profileId);

    // ✅ ADD THIS: Counts the number of logs for a user
    long countByProfileId(Long profileId);

    // ✅ ADD THIS: Finds the oldest log for a user (the first one entered)
    Optional<MenstrualCycleLog> findTopByProfileIdOrderByLogDateAsc(Long profileId);
}