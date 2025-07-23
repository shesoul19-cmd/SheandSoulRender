// package com.sheandsoul.v1update.services;

// import org.springframework.stereotype.Service;

// import com.sheandsoul.v1update.entities.Profile;

// @Service
// public class BreastHealthAssistantService {

//      private final AIService aiService;

//     public BreastHealthAssistantService(AIService aiService) {
//         this.aiService = aiService;
//     }

//     public String handleRequest(String userMessage, Profile userProfile, String language) {
//         // Step 1: Build a single prompt that contains all instructions.
//         String unifiedPrompt = buildUnifiedPrompt(userMessage, userProfile, language);
        
//         // Step 2: Make the single API call to get the final, formatted response.
//         return aiService.getRawLLMResponse(unifiedPrompt);
//     }

//     /**
//      * This prompt instructs the AI on its persona and tells it how to handle
//      * both general questions and requests for the risk report within the same logic.
//      */
//     private String buildUnifiedPrompt(String userMessage, Profile userProfile, String language) {
//         // The AI is smart enough to understand from the user's message if they want their report.
//         String riskLevelInfo = userProfile.getBreastCancerRiskLevel();
//         if (riskLevelInfo == null) {
//             riskLevelInfo = "The user has not completed the risk assessment questionnaire yet.";
//         }

//         return String.format(
//             """
//             You are 'Hope', a compassionate, calm, and supportive breast health assistant for a user named %s.

//             Your instructions are:
//             1.  Analyze the user's question below.
//             2.  The user's calculated risk level from their questionnaire is: '%s'.
//             3.  If the user is asking for their risk report or MCQ results, generate a supportive report based on their risk level. Be calm and proactive, especially for a 'High' risk result.
//             4.  If the user is asking a general question, provide a clear and supportive answer.
//             5.  Your entire response MUST be in natural-sounding %s.
//             6.  You MUST strictly avoid giving medical advice or a diagnosis.
//             7.  You MUST end your response with a clear disclaimer, for example: "This is not medical advice. Please consult with a healthcare professional for diagnosis and treatment."

//             ---
//             User Question: "%s"
//             """,
//             userProfile.getName(), riskLevelInfo, language, userMessage
//         );
//     }

// }
