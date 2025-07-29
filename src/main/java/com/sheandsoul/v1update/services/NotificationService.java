package com.sheandsoul.v1update.services;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.BreastCancerExamLog;
import com.sheandsoul.v1update.entities.Notifications;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.repository.BreastCancerSelfExamLogRepository;
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
    private final BreastCancerSelfExamLogRepository breastCancerSelfExamLogRepository;
    
    @Scheduled(cron = "0 0 9 * * ?") 
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

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void updateCycleDataAutomatically() {
        List<Profile> userProfiles = profileRepository.findByUserType(Profile.UserType.USER);

        for (Profile userProfile : userProfiles) {
            if (userProfile.getLastPeriodStartDate() == null || userProfile.getCycleLength() == null) {
                continue;
            }

            LocalDate nextPeriodStartDate = userProfile.getLastPeriodStartDate().plusDays(userProfile.getCycleLength());
            
            // If the predicted next period date has passed, update the cycle data
            if (nextPeriodStartDate.isBefore(LocalDate.now()) || nextPeriodStartDate.isEqual(LocalDate.now())) {
                // Shift the next predicted cycle to be the new last cycle
                userProfile.setLastPeriodStartDate(nextPeriodStartDate);
                profileRepository.save(userProfile);
            }
        }
    }

    public void updateCycleDataForUser(Profile userProfile) {
        if (userProfile.getLastPeriodStartDate() == null || userProfile.getCycleLength() == null) {
            return;
        }

        LocalDate nextPeriodStartDate = userProfile.getLastPeriodStartDate().plusDays(userProfile.getCycleLength());
        
        // If the predicted next period date has passed, update the cycle data
        if (nextPeriodStartDate.isBefore(LocalDate.now()) || nextPeriodStartDate.isEqual(LocalDate.now())) {
            // Shift the next predicted cycle to be the new last cycle
            userProfile.setLastPeriodStartDate(nextPeriodStartDate);
            profileRepository.save(userProfile);
        }
    }

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkAndSendSelfExamReminder(){
        List<Profile> userProfiles = profileRepository.findByUserType(Profile.UserType.USER);
        
        for(Profile profile : userProfiles){
            Optional<BreastCancerExamLog> latestLogOpt = breastCancerSelfExamLogRepository.findTopByProfileIdOrderByExamDateDesc(profile.getId());
            
            if(latestLogOpt.isPresent()){
                BreastCancerExamLog latestLog = latestLogOpt.get();
                LocalDate lastExamDate = latestLog.getExamDate();
                int reAccessDate = latestLog.getReAccessDate();

                LocalDate nextReminderDate = lastExamDate.plusMonths(reAccessDate);

                if(nextReminderDate == LocalDate.now()){
                    String userMessage = "This is your monthly reminder to perform a breast self-exam. Staying consistent is key to understanding your body.";
                    createNotification(profile, userMessage);
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
