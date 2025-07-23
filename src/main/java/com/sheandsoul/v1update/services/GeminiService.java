package com.sheandsoul.v1update.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.entities.UserServiceType;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getGeminiResponse(String userPrompt) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the request body according to the Gemini API specification
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of(
                    "parts", List.of(
                        Map.of("text", userPrompt)
                    )
                )
            )
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            
            // Navigate through the complex JSON response to find the text
            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null) {
                        List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return parts.get(0).get("text");
                        }
                    }
                }
            }
            return "Sorry, I couldn't process that. Please try again.";

        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return "There was an error connecting to the AI service.";
        }
    }

    private String buildPrompt(User user, String userMessage) {
        Profile profile = user.getProfile();
        UserServiceType serviceType = profile.getPreferredServiceType();

        StringBuilder prompt = new StringBuilder();

        switch (serviceType) {
            case MENSTRUAL:
                prompt.append("You are a menstrual health assistant for a user with: ");
                prompt.append("Age: ").append(profile.getAge()).append(", ");
                prompt.append("Cycle Length: ").append(profile.getCycleLength()).append(" days, ");
                prompt.append("Last Period: ").append(profile.getLastPeriodStartDate()).append(" to ").append(profile.getLastPeriodEndDate()).append(". ");
                prompt.append("User question: ").append(userMessage);
                break;
            case BREAST_CANCER:
                prompt.append("You are a breast cancer awareness assistant. Risk Level: ").append(profile.getBreastCancerRiskLevel()).append(". ");
                prompt.append("User question: ").append(userMessage);
                break;
            case MENTAL_HEALTH:
                prompt.append("You are a mental health assistant. User question: ").append(userMessage);
                break;
            default:
                prompt.append("You are a general health assistant. User question: ").append(userMessage);
        }

        return prompt.toString();
    }

    private String extractTextFromGeminiResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

                if (parts != null && !parts.isEmpty()) {
                    return parts.get(0).get("text");
                }
            }
            return "No content found in response.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to parse response.";
        }
    }

    public String getGeminiResponse(User user, String userMessage) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        String prompt = buildPrompt(user, userMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            )
        );

        HttpEntity<String> entity;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to prepare request.";
        }

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromGeminiResponse(response.getBody());
            } else {
                return "AI service returned an unexpected response.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error connecting to AI service: " + e.getMessage();
        }
    }
}
