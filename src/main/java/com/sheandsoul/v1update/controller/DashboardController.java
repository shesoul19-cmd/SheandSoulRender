package com.sheandsoul.v1update.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.TranslationWrapper;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.services.AppService;
import com.sheandsoul.v1update.services.TranslationService;

@RestController
public class DashboardController {

    @Autowired
    private AppService appService;

    @Autowired
    private TranslationService translationService;

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<?> getDashboard(@PathVariable Long userId) {
        // 1. Fetch the user's profile to get their name and language preference
        Profile userProfile = appService.findProfileByUserId(userId);
        String userLanguage = userProfile.getLanguageCode();

        // 2. Prepare the data for the dashboard
        TranslationWrapper dashboardData = new TranslationWrapper();

        // Add non-translatable data
        dashboardData.addData("userId", userProfile.getId());
        dashboardData.addData("age", userProfile.getAge());
        
        // Add all text that needs to be displayed on the UI
        dashboardData.addUiText("pageTitle", "My Health Dashboard");
        dashboardData.addUiText("welcomeMessage", "Hello, " + userProfile.getName());
        dashboardData.addUiText("cyclePredictionButton", "View Cycle Prediction");
        dashboardData.addUiText("chatButton", "Open AI Assistant");

        // 3. Translate all UI text fields in one go
        Map<String, String> translatedUiText = new HashMap<>();
        for (Map.Entry<String, String> entry : dashboardData.getUiText().entrySet()) {
            String translatedText = translationService.translate(entry.getValue(), userLanguage);
            translatedUiText.put(entry.getKey(), translatedText);
        }
        dashboardData.setUiText(translatedUiText);

        // 4. Return the fully translated data object to the frontend
        return ResponseEntity.ok(dashboardData);
    }

}
