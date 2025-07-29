package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.Profile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenstruationAssistantService {

    private final AppService appService;

    public String handleRequest(String userMessage, Profile userProfile, String language) {
        // Step 1: Use a free, local method to check for a specific, known intent.
        String intent = classifyIntentLocally(userMessage);
        String response;

        if ("GET_CYCLE_PREDICTION".equals(intent)) {
            // This case is handled internally without any API call. It's fast and free.
            response = appService.getCyclePredictionAsText(userProfile.getUser().getId());
        } else {
            // For other cases, we would need an AI service, but that's outside the scope of this task
            response = "I can help you with menstrual cycle predictions. Please ask about your next period date.";
        }
        
        // The response is already refined, so no extra processing is needed.
        return response;
    }

    /**
     * A simple, free, and fast keyword-based check.
     * This avoids making an unnecessary API call for a predictable query.
     */
    private String classifyIntentLocally(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase();
        // You can add more keywords here
        if (lowerCaseMessage.contains("predict") || lowerCaseMessage.contains("next period") || lowerCaseMessage.contains("cycle")) {
            return "GET_CYCLE_PREDICTION";
        }
        return "GENERAL_QUESTION";
    }
}
