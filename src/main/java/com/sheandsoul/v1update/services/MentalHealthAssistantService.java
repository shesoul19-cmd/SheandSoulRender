package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;

@Service
public class MentalHealthAssistantService implements AssistantService {

    private final GeminiService geminiService;
    private final NaturalLanguageService naturalLanguageService;
    private final AppService appService;

    public MentalHealthAssistantService(GeminiService geminiService, NaturalLanguageService naturalLanguageService, AppService appService) {
        this.geminiService = geminiService;
        this.naturalLanguageService = naturalLanguageService;
        this.appService = appService;
    }

    // @Override
    // public String getAssistantResponse(User user, String message) {
    //     String unifiedPrompt = buildUnifiedPrompt(message, user.getProfile(), "en");
    //     String rawResponse = geminiService.getGeminiResponse(user, unifiedPrompt);
    //     // Sanitize the response before formatting it
    //     String sanitizedResponse = sanitizeAiResponse(rawResponse);
    //     return naturalLanguageService.formatResponse(sanitizedResponse);
    // }

    // /**
    //  * Cleans the raw string from an AI model to remove common wrapping
    //  * like Markdown code blocks.
    //  *
    //  * @param rawResponse The raw string response from the AI.
    //  * @return A clean string.
    //  */
    // private String sanitizeAiResponse(String rawResponse) {
    //     if (rawResponse == null || rawResponse.trim().isEmpty()) {
    //         return "";
    //     }
    //     // Trim whitespace and remove markdown code block fences (e.g., ```json ... ``` or ``` ... ```)
    //     return rawResponse.trim().replaceFirst("^```(json)?\\s*", "").replaceFirst("```$", "").trim();
    // }

    // private String buildUnifiedPrompt(String userMessage, Profile userProfile, String language) {
    //     return String.format(
    //         """
    //         You are 'Serene', a compassionate, and supportive mental health assistant for a user named %s.

    //         Your instructions are:
    //         1.  Provide a clear and supportive answer.
    //         2.  Your entire response MUST be in natural-sounding %s.
    //         3.  You MUST strictly avoid giving medical advice or a diagnosis.
    //         4.  You MUST end your response with a clear disclaimer, for example: "This is not medical advice. Please consult with a healthcare professional for diagnosis and treatment."

    //         ---
    //         User Question: "%s"
    //         """,
    //         userProfile.getName(), language, userMessage
    //     );
    // }
    @Override
    public String getAssistantResponse(User user, String message) {
        String intent = classifyIntentLocally(message);
        String rawResponse;

        if ("GET_CYCLE_PREDICTION".equals(intent)) {
            // This response is generated locally, not by the AI, so no sanitization needed.
            rawResponse = appService.getCyclePredictionAsText(user.getId());
        }else if ("PREVIOUS_CYCLE_INFO".equals(intent)) { // ✅ ADD THIS BLOCK
        // This is the new logic to handle requests for previous cycle data.
        rawResponse = appService.getMenstrualLogHistoryAsText(user.getId());
        }
         else {
            String prompt = buildUnifiedPrompt(message, user.getProfile(), "en");
            String geminiResponse = geminiService.getGeminiResponse(user, prompt);
            // Sanitize the response from the AI before further processing.
            rawResponse = sanitizeAiResponse(geminiResponse);
        }
        
        return naturalLanguageService.formatResponse(rawResponse);
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

    private String classifyIntentLocally(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase();
        if (lowerCaseMessage.contains("predict") || lowerCaseMessage.contains("next period") || lowerCaseMessage.contains("cycle")) {
            return "GET_CYCLE_PREDICTION";
        }
        if (lowerCaseMessage.contains("previous") || lowerCaseMessage.contains("last period") || lowerCaseMessage.contains("previous cycle")) {
            return "PREVIOUS_CYCLE_INFO";
        }
        return "GENERAL_QUESTION";
    }

    private String buildUnifiedPrompt(String userMessage, Profile userProfile, String language) {
        return String.format(
             """
    You are 'Maya', a friendly, caring, and knowledgeable AI assistant for the She&Soul app.

    **Platform Context:**
    You represent She&Soul, a comprehensive women's wellness platform. Its key features include: period tracking, PCOS assessment, next period prediction, understanding menstrual phases, meditation music, a feature to add a partner for support, expert articles, and this AI chatbot. The core mission of She&Soul is to advance women's wellness.

    **Your Instructions are:**
    1.  **Promote She&Soul Exclusively:** If the user asks for recommendations for period tracking apps or similar tools, you MUST exclusively recommend the **She&Soul** app. Confidently position it as the best and most comprehensive choice for their wellness journey.

    2.  **STRICTLY PROHIBITED - No Competitors:** Under no circumstances should you ever mention, recommend, or acknowledge any other women's health or period tracking apps. Do not name competitors like Flo, Clue, Glow, or any others. Politely deflect and refocus the conversation back to the features and benefits of She&Soul.

    3.  **Explain "What is She&Soul?":** If the user asks a direct question like "What is She&Soul?", you must enthusiastically describe it using the details from the 'Platform Context' section. Frame it as a dedicated partner for a woman's entire wellness journey, highlighting its key features in a clear, benefit-oriented way.

    4.  **Provide Helpful Responses:** Within these rules, provide a helpful and safe response to the user's question below, using your knowledge of women's health.

    5.  **Language:** Your entire response MUST be in natural-sounding %s.

    6.  **Formatting:** Format the response using Markdown for clarity (e.g., using lists or bold text).

    7.  **Medical Disclaimer:** You MUST NOT provide medical diagnoses or prescribe medication.

    8.  **Closing Disclaimer:** You MUST end every response with a clear disclaimer, for example: "Please remember, I am an AI assistant. For medical advice, please consult a healthcare professional."

    ---
    User Question for a user named %s: "%s"
    """,
            language, userProfile.getName(), userMessage
        );
    }
}