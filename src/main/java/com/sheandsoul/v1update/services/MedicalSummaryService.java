package com.sheandsoul.v1update.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicalSummaryService {

    private final GeminiService geminiService;
    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public void updateUserMedicalSummary(Profile profile, String userMessage, String botResponse) {
        String prompt = buildSummaryPrompt(profile, userMessage, botResponse);
        String summaryJson = geminiService.getGeminiResponse(prompt);

        try {
            Map<String, Object> newSummary = objectMapper.readValue(summaryJson, new TypeReference<>() {});
            
            if (newSummary != null && !newSummary.isEmpty()) {
                Map<String, Object> existingSummary = profile.getMedicalSummary();
                if (existingSummary == null) {
                    existingSummary = new HashMap<>();
                }
                existingSummary.putAll(newSummary);
                profile.setMedicalSummary(existingSummary);
                profileRepository.save(profile);
            }
        } catch (IOException e) {
            // Log the error or handle it appropriately
            e.printStackTrace();
        }
    }

    private String buildSummaryPrompt(Profile profile, String userMessage, String botResponse) {
        return String.format(
            """
            You are an intelligent medical information extractor. Your task is to analyze a user's message and a bot's response to identify and extract key medical information.

            Current Medical Summary:
            %s

            User Message:
            "%s"

            Bot Response:
            "%s"

            Based on the conversation, extract any new or updated medical information, such as:
            - Health issues or conditions (e.g., PCOS, diabetes, hypertension)
            - Food allergies (e.g., peanuts, gluten)
            - Medication allergies (e.g., penicillin)
            - Other important medical history (e.g., surgeries, chronic illnesses)

            Respond with ONLY a JSON object containing the extracted information. For example:
            {
              "health_conditions": ["PCOS", "Anemia"],
              "allergies": {
                "food": ["Peanuts"],
                "medication": []
              }
            }
            If no new medical information is found, respond with an empty JSON object: {}
            """,
            profile.getMedicalSummary() != null ? profile.getMedicalSummary().toString() : "{}",
            userMessage,
            botResponse
        );
    }
}
