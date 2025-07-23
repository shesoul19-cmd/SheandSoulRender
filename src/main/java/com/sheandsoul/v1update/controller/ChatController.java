package com.sheandsoul.v1update.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.ChatRequest;
import com.sheandsoul.v1update.dto.ChatResponse;
import com.sheandsoul.v1update.entities.ChatHistory;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.repository.ChatHistoryRepository;
import com.sheandsoul.v1update.services.GeminiService;
import com.sheandsoul.v1update.services.MyUserDetailService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {


    private final ChatHistoryRepository chatHistoryRepository;

    private final GeminiService geminiService;
    private final MyUserDetailService userDetailsService;

    public ChatController(GeminiService geminiService, MyUserDetailService userDetailsService, ChatHistoryRepository chatHistoryRepository){
        this.chatHistoryRepository = chatHistoryRepository;
        this.geminiService = geminiService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request, Authentication auth) {
        try {
            User currentUser = userDetailsService.findUserByEmail(auth.getName());

            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User profile not found. Please create a profile first."));
            }

            // Delegate the entire logic to the AssistantService
            String aiResponse =geminiService.getGeminiResponse(currentUser, request.getMessage());
            ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUser(currentUser);
        chatHistory.setUserMessage(request.getMessage());
        chatHistory.setBotResponse(aiResponse);
        chatHistory.setTimestamp(LocalDateTime.now());
        chatHistory.setServiceType(currentUser.getProfile().getPreferredServiceType());

        chatHistoryRepository.save(chatHistory);
            return ResponseEntity.ok(new ChatResponse(aiResponse));

        } catch (Exception e) {
            // Log the error for debugging: e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }

}
