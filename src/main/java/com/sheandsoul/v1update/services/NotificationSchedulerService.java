package com.sheandsoul.v1update.services;

import com.sheandsoul.v1update.dto.CyclePredictionDto;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);

    private final ProfileRepository profileRepository;
    private final AppService appService;
    private final NotificationService notificationService;

    public NotificationSchedulerService(
            ProfileRepository profileRepository,
            AppService appService,
            NotificationService notificationService
    ) {
        this.profileRepository = profileRepository;
        this.appService = appService;
        this.notificationService = notificationService;
    }

    /**
     * This job runs every day at 9:00 AM server time.
     * Cron format: "second minute hour day-of-month month day-of-week"
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyPeriodReminders() {
        logger.info("Starting daily period reminder job...");

        // 1. Get all profiles of type USER who have completed onboarding
        List<Profile> userProfiles = profileRepository.findByUserType(Profile.UserType.USER);

        LocalDate today = LocalDate.now();

        // 2. Loop through each profile
        for (Profile profile : userProfiles) {
            // Skip users who haven't finished onboarding or have notifications disabled (add this check later)
            if (profile.getLastPeriodStartDate() == null || profile.getCycleLength() == null) {
                continue;
            }

            try {
                User user = profile.getUser();
                CyclePredictionDto prediction = appService.predictNextCycle(user.getId());
                LocalDate nextPeriodStartDate = prediction.getNextPeriodStartDate();

                // 3. Calculate days until the next period
                long daysLeft = ChronoUnit.DAYS.between(today, nextPeriodStartDate);

                // 4. Check if the user is in the 7-day notification window
                if (daysLeft >= 0 && daysLeft <= 7) {
                    
                    // 5. Check if the user has a device token
                    String deviceToken = profile.getDeviceToken();
                    if (deviceToken == null || deviceToken.isBlank()) {
                        logger.warn("User {} has no device token. Skipping notification.", user.getEmail());
                        continue;
                    }

                    // 6. Construct and send the notification
                    String title = "Period Reminder";
                    String body = daysLeft == 0
                        ? "Hi " + profile.getName() + ", your period is predicted to start today."
                        : "Hi " + profile.getName() + ", your period is predicted in " + daysLeft + " " + (daysLeft == 1 ? "day" : "days") + ".";
                    
                    notificationService.sendNotification(deviceToken, title, body);
                    logger.info("Sent period reminder to user {}. Days left: {}", user.getEmail(), daysLeft);
                }

            } catch (Exception e) {
                // Important: Catch exceptions for each user so one failure doesn't stop the whole job
                logger.error("Failed to process period reminder for user ID {}: {}", profile.getUser().getId(), e.getMessage());
            }
        }
        logger.info("Finished daily period reminder job.");
    }
}