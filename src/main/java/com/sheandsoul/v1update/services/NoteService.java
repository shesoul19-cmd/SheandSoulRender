package com.sheandsoul.v1update.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sheandsoul.v1update.dto.UserNoteDto;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.UserNote;
import com.sheandsoul.v1update.repository.ProfileRepository;
import com.sheandsoul.v1update.repository.UserNoteRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NoteService {

    private final UserNoteRepository noteRepository;
    private final ProfileRepository profileRepository;

    public NoteService(UserNoteRepository noteRepository, ProfileRepository profileRepository) {
        this.noteRepository = noteRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public UserNoteDto createNote(Long userId, String content) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        
        UserNote note = new UserNote();
        note.setProfile(profile);
        note.setContent(content);
        
        UserNote savedNote = noteRepository.save(note);
        return toDto(savedNote);
    }

    public List<UserNoteDto> getNotesForUser(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));

        return noteRepository.findByProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserNoteDto updateNote(Long noteId, String content, Long userId) {
        UserNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with ID: " + noteId));

        if (!note.getProfile().getUser().getId().equals(userId)) {
            throw new SecurityException("User not authorized to update this note.");
        }

        note.setContent(content);
        UserNote updatedNote = noteRepository.save(note);
        return toDto(updatedNote);
    }

    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        UserNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with ID: " + noteId));

        if (!note.getProfile().getUser().getId().equals(userId)) {
            throw new SecurityException("User not authorized to delete this note.");
        }

        noteRepository.delete(note);
    }

    private UserNoteDto toDto(UserNote note) {
        UserNoteDto dto = new UserNoteDto();
        dto.setId(note.getId());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
}