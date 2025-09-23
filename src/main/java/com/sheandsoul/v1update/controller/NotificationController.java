package com.sheandsoul.v1update.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.services.NotificationService;

record NotificationRequest(String token, String title, String body) {}

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendTestNotification(@RequestBody NotificationRequest request) {
        try {
            notificationService.sendNotification(request.token(), request.title(), request.body());
            return ResponseEntity.ok(Map.of("message", "Notification sent successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send notification: " + e.getMessage()));
        }
    }
}