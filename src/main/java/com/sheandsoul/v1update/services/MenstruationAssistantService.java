package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.Profile;

@Service
public class MenstruationAssistantService {

    private final AIService aiService;
    private final AppService appService;

    public MenstruationAssistantService(AIService aiService, AppService appService) {
        this.aiService = aiService;
        this.appService = appService;
    }

    public String handleRequest(String userMessage, Profile userProfile, String language) {
        // Step 1: Use a free, local method to check for a specific, known intent.
        String intent = classifyIntentLocally(userMessage);
        String response;

        if ("GET_CYCLE_PREDICTION".equals(intent)) {
            // This case is handled internally without any API call. It's fast and free.
            response = appService.getCyclePredictionAsText(userProfile.getUser().getId());
        } else {
            // Step 2: For all other general questions, build a single, powerful prompt.
            String prompt = buildUnifiedPrompt(userMessage, userProfile, language);
            // Step 3: Make only ONE API call to Gemini.
            response = aiService.getRawLLMResponse(prompt);
        }
        
        // The response from the AI is already refined, so no extra processing is needed.
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

    /**
     * This single prompt tells the AI its persona, how to format the answer,
     * and what the user is asking. It replaces the three separate API calls.
     */
    private String buildUnifiedPrompt(String userMessage, Profile userProfile, String language) {
        return String.format(
            """
            You are 'Maya', a friendly, caring, and knowledgeable menstrual health assistant for a user named %s.

            Your instructions are:
            1.  Provide a helpful and safe response to the user's question below.
            2.  Your entire response MUST be in natural-sounding %s.
            3.  Format the response using Markdown for clarity (e.g., using lists or bold text).
            4.  You MUST NOT provide medical diagnoses or prescribe medication.
            5.  You MUST end your response with a clear disclaimer, for example: "Please remember, I am an AI assistant. Consult a healthcare professional for medical advice."

            ---
            User Question: "%s"
            """,
            userProfile.getName(), language, userMessage
        );
    }
}
