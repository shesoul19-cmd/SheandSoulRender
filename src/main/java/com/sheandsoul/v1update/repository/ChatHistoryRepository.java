package com.sheandsoul.v1update.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.ChatHistory;
import java.util.List;
import com.sheandsoul.v1update.entities.User;


public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserOrderByTimestamp(User user);
}
