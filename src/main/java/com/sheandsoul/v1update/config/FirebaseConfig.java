package com.sheandsoul.v1update.config;

import org.springframework.context.annotation.Configuration;

import com.google.firebase.FirebaseApp;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // The SDK will automatically find the credentials via the
                // GOOGLE_APPLICATION_CREDENTIALS environment variable you set in Render.
                FirebaseApp.initializeApp();
            }
        } catch (Exception e) {
            // This will help you debug if something goes wrong on Render
            System.err.println("Firebase initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}