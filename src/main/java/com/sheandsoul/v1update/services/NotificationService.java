package com.sheandsoul.v1update.services;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.Notifications;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.repository.NotificationRepository;
import com.sheandsoul.v1update.repository.ProfileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final AppService appService;
    private final NotificationRepository notificationRepository;

     @Scheduled(cron = "0 0 9 * * ?") // Runs daily at 2:00 AM
    @Transactional
    public void checkAndSendPeriodAlerts() {
        List<Profile> userProfiles = profileRepository.findByUserType(Profile.UserType.USER);

        for (Profile userProfile : userProfiles) {
            if (userProfile.getLastPeriodStartDate() == null || userProfile.getCycleLength() == null) {
                continue; 
            }

            LocalDate nextPeriodStartDate = userProfile.getLastPeriodStartDate().plusDays(userProfile.getCycleLength());
            long daysUntilPeriod = ChronoUnit.DAYS.between(LocalDate.now(), nextPeriodStartDate);

            if (daysUntilPeriod >= 0 && daysUntilPeriod <= 7) {
                String dayString;
                if (daysUntilPeriod == 0) {
                    dayString = "today";
                } else if (daysUntilPeriod == 1) {
                    dayString = "tomorrow";
                } else {
                    dayString = "in " + daysUntilPeriod + " days";
                }

                String userMessage = String.format("Heads up! Your period is predicted to start %s.", dayString);
                createNotification(userProfile, userMessage);

                if (userProfile.getReferralCode() != null) {
                    profileRepository.findByReferralCode(userProfile.getReferralCode())
                        .ifPresent(partnerProfile -> {
                            String partnerMessage = String.format("Heads up! %s's period is predicted to start %s.", userProfile.getName(), dayString);
                            createNotification(partnerProfile, partnerMessage);
                        });
                }
            }
        }
    }
    private void createNotification(Profile profile, String message) {
        Notifications notification = new Notifications();
        notification.setProfile(profile);
        notification.setMessage(message);
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public List<Notifications> getUnreadNotifications(Long userId) {
        Profile userProfile = appService.findProfileByUserId(userId);
        return notificationRepository.findByProfileIdAndIsReadFalseOrderByCreatedAtDesc(userProfile.getId());
    }
}
