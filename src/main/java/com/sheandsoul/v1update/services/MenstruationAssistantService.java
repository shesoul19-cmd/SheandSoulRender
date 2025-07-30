package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;

@Service
public class MenstruationAssistantService implements AssistantService {

    private final AppService appService;
    private final GeminiService geminiService;
    private final NaturalLanguageService naturalLanguageService;

    public MenstruationAssistantService(AppService appService, GeminiService geminiService, NaturalLanguageService naturalLanguageService) {
        this.appService = appService;
        this.geminiService = geminiService;
        this.naturalLanguageService = naturalLanguageService;
    }

    @Override
    public String getAssistantResponse(User user, String message) {
        String intent = classifyIntentLocally(message);
        String rawResponse;

        if ("GET_CYCLE_PREDICTION".equals(intent)) {
            rawResponse = appService.getCyclePredictionAsText(user.getId());
        } else {
            String prompt = buildUnifiedPrompt(message, user.getProfile(), "en");
            rawResponse = geminiService.getGeminiResponse(user, prompt);
        }
        
        return naturalLanguageService.formatResponse(rawResponse);
    }

    private String classifyIntentLocally(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase();
        if (lowerCaseMessage.contains("predict") || lowerCaseMessage.contains("next period") || lowerCaseMessage.contains("cycle")) {
            return "GET_CYCLE_PREDICTION";
        }
        return "GENERAL_QUESTION";
    }

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
