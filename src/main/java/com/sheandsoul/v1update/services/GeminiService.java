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

    private String buildPrompt(User user, String userMessage) {
        Profile profile = user.getProfile();
        if (profile == null) {
            return String.format("You are a general health assistant. User question: %s", userMessage);
        }

        UserServiceType serviceType = profile.getPreferredServiceType();
        if (serviceType == null) {
            serviceType = UserServiceType.MENTAL_HEALTH; // Default service type
        }
    
        StringBuilder prompt = new StringBuilder();

        switch (serviceType) {
            case MENSTRUAL:
                prompt.append("You are a menstrual health assistant for a user. ");
                if (profile.getAge() != null) prompt.append("Age: ").append(profile.getAge()).append(". ");
                if (profile.getCycleLength() != null) prompt.append("Cycle Length: ").append(profile.getCycleLength()).append(" days. ");
                if (profile.getLastPeriodStartDate() != null) prompt.append("Last Period Start: ").append(profile.getLastPeriodStartDate()).append(". ");
                if (profile.getLastPeriodEndDate() != null) prompt.append("Last Period End: ").append(profile.getLastPeriodEndDate()).append(". ");
                break;
            case BREAST_CANCER:
                prompt.append("You are a breast cancer awareness assistant. ");
                if (profile.getBreastCancerRiskLevel() != null) prompt.append("Risk Level: ").append(profile.getBreastCancerRiskLevel()).append(". ");
                break;
            case MENTAL_HEALTH:
                prompt.append("You are a mental health assistant. ");
                break;
            default:
                prompt.append("You are a general health assistant. ");
                break;
        }

        prompt.append("User question: ").append(userMessage);
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

    public String getGeminiResponse(String userPrompt) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", userPrompt)))
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

    public String getGeminiResponse(User user, String userMessage) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

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
