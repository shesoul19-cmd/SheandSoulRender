package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;

@Service
public class MentalHealthAssistantService implements AssistantService {

    private final GeminiService geminiService;
    private final NaturalLanguageService naturalLanguageService;

    public MentalHealthAssistantService(GeminiService geminiService, NaturalLanguageService naturalLanguageService) {
        this.geminiService = geminiService;
        this.naturalLanguageService = naturalLanguageService;
    }

    @Override
    public String getAssistantResponse(User user, String message) {
        String unifiedPrompt = buildUnifiedPrompt(message, user.getProfile(), "en");
        String rawResponse = geminiService.getGeminiResponse(user, unifiedPrompt);
        return naturalLanguageService.formatResponse(rawResponse);
    }

    private String buildUnifiedPrompt(String userMessage, Profile userProfile, String language) {
        return String.format(
            """
            You are 'Serene', a compassionate, and supportive mental health assistant for a user named %s.

            Your instructions are:
            1.  Provide a clear and supportive answer.
            2.  Your entire response MUST be in natural-sounding %s.
            3.  You MUST strictly avoid giving medical advice or a diagnosis.
            4.  You MUST end your response with a clear disclaimer, for example: "This is not medical advice. Please consult with a healthcare professional for diagnosis and treatment."

            ---
            User Question: "%s"
            """,
            userProfile.getName(), language, userMessage
        );
    }
}
