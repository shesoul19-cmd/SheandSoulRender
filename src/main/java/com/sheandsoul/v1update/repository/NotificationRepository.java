package com.sheandsoul.v1update.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.Notifications;

public interface NotificationRepository extends JpaRepository<Notifications, Long>{
     List<Notifications> findByProfileIdAndIsReadFalseOrderByCreatedAtDesc(Long profileId);
}
