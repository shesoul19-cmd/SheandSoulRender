package com.sheandsoul.v1update.controller;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict; // <-- ADD THIS IMPORT
import org.springframework.cache.annotation.Cacheable;  // <-- ADD THIS IMPORT
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.NoteRequestDto;
import com.sheandsoul.v1update.dto.UserNoteDto;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.services.NoteService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final MyUserDetailService userDetailsService;

    public NoteController(NoteService noteService, MyUserDetailService userDetailsService) {
        this.noteService = noteService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    // ✅ NEW: Clears the "userNotes" cache for this user
    @CacheEvict(value = "userNotes", key = "#authentication.name")
    public ResponseEntity<UserNoteDto> createNote(@Valid @RequestBody NoteRequestDto request, Authentication authentication) {
        User currentUser = userDetailsService.findUserByEmail(authentication.getName());
        UserNoteDto createdNote = noteService.createNote(currentUser.getId(), request);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }


    @GetMapping
    // ✅ NEW: Caches the result in "userNotes" using the user's email as the key
    @Cacheable(value = "userNotes", key = "#authentication.name")
    public ResponseEntity<List<UserNoteDto>> getNotes(Authentication authentication) {
        User currentUser = userDetailsService.findUserByEmail(authentication.getName());
        List<UserNoteDto> notes = noteService.getNotesForUser(currentUser.getId());
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/{id}")
    // ✅ NEW: Clears the "userNotes" cache for this user
    @CacheEvict(value = "userNotes", key = "#authentication.name")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @Valid @RequestBody NoteRequestDto request, Authentication authentication) {
        User currentUser = userDetailsService.findUserByEmail(authentication.getName());
        try {
            UserNoteDto updatedNote = noteService.updateNote(id, request, currentUser.getId());
            return ResponseEntity.ok(updatedNote);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    // ✅ NEW: Clears the "userNotes" cache for this user
    @CacheEvict(value = "userNotes", key = "#authentication.name")
    public ResponseEntity<?> deleteNote(@PathVariable Long id, Authentication authentication) {
        User currentUser = userDetailsService.findUserByEmail(authentication.getName());
        try {
            noteService.deleteNote(id, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }
}