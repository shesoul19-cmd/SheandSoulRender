package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;

@Service
public class BreastHealthAssistantService implements AssistantService {

    private final GeminiService geminiService;
    private final NaturalLanguageService naturalLanguageService;

    public BreastHealthAssistantService(GeminiService geminiService, NaturalLanguageService naturalLanguageService) {
        this.geminiService = geminiService;
        this.naturalLanguageService = naturalLanguageService;
    }

    @Override
    public String getAssistantResponse(User user, String message) {
        String unifiedPrompt = buildUnifiedPrompt(message, user.getProfile(), "en");
        String rawResponse = geminiService.getGeminiResponse(user, unifiedPrompt);
        // Sanitize the response before formatting it
        String sanitizedResponse = sanitizeAiResponse(rawResponse);
        return naturalLanguageService.formatResponse(sanitizedResponse);
    }

    /**
     * Cleans the raw string from an AI model to remove common wrapping
     * like Markdown code blocks.
     *
     * @param rawResponse The raw string response from the AI.
     * @return A clean string.
     */
    private String sanitizeAiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return "";
        }
        // Trim whitespace and remove markdown code block fences (e.g., ```json ... ``` or ``` ... ```)
        return rawResponse.trim().replaceFirst("^```(json)?\\s*", "").replaceFirst("```$", "").trim();
    }

    private String buildUnifiedPrompt(String userMessage, Profile userProfile, String language) {
        String riskLevelInfo = userProfile.getBreastCancerRiskLevel();
        if (riskLevelInfo == null) {
            riskLevelInfo = "The user has not completed the risk assessment questionnaire yet.";
        }

        return String.format(
            """
            You are 'Hope', a compassionate, calm, and supportive breast health assistant for a user named %s.

            Your instructions are:
            1.  Analyze the user's question below.
            2.  The user's calculated risk level from their questionnaire is: '%s'.
            3.  If the user is asking for their risk report or MCQ results, generate a supportive report based on their risk level. Be calm and proactive, especially for a 'High' risk result.
            4.  If the user is asking a general question, provide a clear and supportive answer.
            5.  Your entire response MUST be in natural-sounding %s.
            6.  You MUST strictly avoid giving medical advice or a diagnosis.
            7.  You MUST end your response with a clear disclaimer, for example: "This is not medical advice. Please consult with a healthcare professional for diagnosis and treatment."

            ---
            User Question: "%s"
            """,
            userProfile.getName(), riskLevelInfo, language, userMessage
        );
    }
}