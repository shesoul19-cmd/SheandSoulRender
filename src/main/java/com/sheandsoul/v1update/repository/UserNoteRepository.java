package com.sheandsoul.v1update.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.UserNote;

public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    List<UserNote> findByProfileIdOrderByCreatedAtDesc(Long profileId);
}