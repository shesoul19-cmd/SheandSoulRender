package com.sheandsoul.v1update.controller;

import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import com.sheandsoul.v1update.entities.Notifications;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final MyUserDetailService myUserDetailsService;

    public NotificationController(NotificationService notificationService, MyUserDetailService myUserDetailsService) {
        this.notificationService = notificationService;
        this.myUserDetailsService = myUserDetailsService;
    }

    @GetMapping("/me/unread")
    public ResponseEntity<List<Notifications>> getMyUnreadNotifications(Authentication authentication) {
        User currentUser = myUserDetailsService.findUserByEmail(authentication.getName());
        List<Notifications> notifications = notificationService.getUnreadNotifications(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    // @PostMapping("/schedule/period")
    // public ResponseEntity<String> schedulePeriodNotifications(@RequestParam Long userId, @RequestParam String nextPeriodDate) {
    //     LocalDate startDate = LocalDate.parse(nextPeriodDate);
    //     notificationService.checkAndSendPeriodAlerts(userId, startDate);
    //     return ResponseEntity.ok("Period notifications scheduled successfully.");
    // }

    // @PostMapping("/schedule/breast-cancer")
    // public ResponseEntity<String> scheduleBreastCancerNotification(@RequestParam Long userId, @RequestParam String reassessmentDate) {
    //     LocalDate notificationDate = LocalDate.parse(reassessmentDate);
    //     notificationService.scheduleBreastCancerNotification(userId, notificationDate);
    //     return ResponseEntity.ok("Breast cancer notification scheduled successfully.");
    // }
}
