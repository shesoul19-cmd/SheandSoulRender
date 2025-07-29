package com.sheandsoul.v1update.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    public void sendPush(String deviceToken, String title, String body) {
        if (deviceToken == null || deviceToken.isEmpty()) {
            return;  // Skip if no token
        }

        Message message = Message.builder()
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .setToken(deviceToken)
            .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            System.out.println("Push sent successfully!");  // Simple logging
        } catch (FirebaseMessagingException e) {
            System.err.println("Error sending push: " + e.getMessage());
        }
    }
}
