package com.sheandsoul.v1update.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.TranslationWrapper;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.services.TranslationService;

@RestController
public class DashboardController {

    @Autowired
    private MyUserDetailService myUserDetailsService;

    @Autowired
    private TranslationService translationService;

    @GetMapping("/api/dashboard/me") // Changed URL to a secure, standard convention
    public ResponseEntity<?> getMyDashboard(Authentication authentication) { // Changed method signature
        
        // 1. Securely get the user's identity from the authentication token
        String userEmail = authentication.getName();
        User currentUser = myUserDetailsService.findUserByEmail(userEmail);
        Profile userProfile = currentUser.getProfile(); // Get profile from the user entity

        if (userProfile == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User profile not found. Please create a profile first."));
        }

        String userLanguage = userProfile.getLanguageCode();

        // 2. Prepare the data for the dashboard
        TranslationWrapper dashboardData = new TranslationWrapper();

        // Add non-translatable data
        dashboardData.addData("userId", currentUser.getId());
        dashboardData.addData("profileId", userProfile.getId());
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
