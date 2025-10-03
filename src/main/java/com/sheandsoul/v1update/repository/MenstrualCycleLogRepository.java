package com.sheandsoul.v1update.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.MenstrualCycleLog;

public interface MenstrualCycleLogRepository extends JpaRepository<MenstrualCycleLog, Long> {
}