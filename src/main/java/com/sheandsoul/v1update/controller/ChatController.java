package com.sheandsoul.v1update.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.ChatRequest;
import com.sheandsoul.v1update.dto.ChatResponse;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.AssistantService;
import com.sheandsoul.v1update.services.AssistantServiceFactory;
import com.sheandsoul.v1update.services.MedicalSummaryService;
import com.sheandsoul.v1update.services.MyUserDetailService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final MyUserDetailService userDetailsService;
    private final AssistantServiceFactory assistantServiceFactory;
    private final MedicalSummaryService medicalSummaryService;

    public ChatController(MyUserDetailService userDetailsService, AssistantServiceFactory assistantServiceFactory, MedicalSummaryService medicalSummaryService) {
        this.userDetailsService = userDetailsService;
        this.assistantServiceFactory = assistantServiceFactory;
        this.medicalSummaryService = medicalSummaryService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request, Authentication auth) {
        try {
            User currentUser = userDetailsService.findUserByEmail(auth.getName());

            if (currentUser == null || currentUser.getProfile() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User profile not found. Please create a profile first."));
            }

            AssistantService assistant = assistantServiceFactory.getAssistant(currentUser.getProfile().getPreferredServiceType());
            String aiResponse = assistant.getAssistantResponse(currentUser, request.getMessage());

            // Instead of saving the full chat history, update the medical summary
            medicalSummaryService.updateUserMedicalSummary(currentUser.getProfile(), request.getMessage(), aiResponse);

            return ResponseEntity.ok(new ChatResponse(aiResponse));

        } catch (Exception e) {
            // Log the error for debugging: e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }
}
