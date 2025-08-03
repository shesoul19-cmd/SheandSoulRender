package com.sheandsoul.v1update.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sheandsoul.v1update.dto.CyclePredictionDto;
import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.dto.MenstrualTrackingDto;
import com.sheandsoul.v1update.dto.PartnerDataDto;
import com.sheandsoul.v1update.dto.ProfileRequest;
import com.sheandsoul.v1update.dto.ProfileResponse;
import com.sheandsoul.v1update.dto.ProfileServiceDto;
import com.sheandsoul.v1update.dto.SignUpRequest;
import com.sheandsoul.v1update.entities.BreastCancerExamLog;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.SymptomLocation;
import com.sheandsoul.v1update.entities.SymptomSide;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.entities.UserServiceType;
import com.sheandsoul.v1update.repository.BreastCancerSelfExamLogRepository;
import com.sheandsoul.v1update.repository.ProfileRepository;
import com.sheandsoul.v1update.repository.UserRepository;

@Service
public class AppService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReferralCodeService referralCodeService;
    private final OtpGenerationService otpGenerationService;
    private final EmailService emailService;
    private final BreastCancerSelfExamLogRepository selfExamLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(AppService.class);

    public AppService(UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder, ReferralCodeService referralCodeService, OtpGenerationService otpGenerationService, EmailService emailService, BreastCancerSelfExamLogRepository selfExamLogRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.referralCodeService = referralCodeService;
        this.otpGenerationService = otpGenerationService;
        this.emailService = emailService;
        this.selfExamLogRepository = selfExamLogRepository;
    }

    @Transactional
    public User registerUser(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            logger.warn("Registration attempt with existing email: {}", request.email());
            throw new IllegalStateException("Email already in use");
        }
        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setEmailVerified(false);

        User savedUser = userRepository.save(newUser);
        logger.info("User {} saved successfully with ID: {}", savedUser.getEmail(), savedUser.getId());

        try {
            logger.info("Initiating OTP process for user: {}", savedUser.getEmail());
            String otp = otpGenerationService.generateOtp();
            otpGenerationService.storeOtp(savedUser.getEmail(), otp);
            emailService.sendOtpEmail(savedUser.getEmail(), otp);
            logger.info("OTP email process completed for user: {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("OTP process failed for user {} after registration.", savedUser.getEmail(), e);
            // Don't re-throw; user is registered and can request OTP resend.
        }

        return savedUser;
        
    }

    @Transactional
    public void verifyEmail(String email, String submittedOtp){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
        if(user.isEmailVerified()) {
            logger.warn("Attempt to verify an already-verified email: {}", email);
            throw new IllegalStateException("Email is already verified.");
        }

        if (!otpGenerationService.isOtpValid(email, submittedOtp)) {
            logger.warn("Invalid OTP '{}' submitted for email: {}", submittedOtp, email);
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        logger.info("Email successfully verified for: {}", email);

        otpGenerationService.markOtpAsUsed(email);
    }

    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            logger.warn("OTP resend requested for already-verified email: {}", email);
            throw new IllegalStateException("Email is already verified.");
        }

        logger.info("Resending OTP for user: {}", user.getEmail());
        String otp = otpGenerationService.generateOtp();
        otpGenerationService.storeOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), otp);
        logger.info("OTP resent successfully for user: {}", user.getEmail());
    }

    @Transactional
    public ProfileResponse createProfile(ProfileRequest request, User user) {
        if (user.getProfile() != null) {
            throw new IllegalStateException("User already has a profile.");
        }

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setName(request.name());
        profile.setNickName(request.nickname());
        profile.setUserType(request.userType());

        if (request.userType() == Profile.UserType.USER) {
            profile.setAge(request.age());
            profile.setHeight(request.height());
            profile.setWeight(request.weight());
            // Set preferredServiceType only if provided, can be null initially
            if (request.preferredServiceType() != null) {
                profile.setPreferredServiceType(request.preferredServiceType());
            }
            
            Profile savedProfile = profileRepository.saveAndFlush(profile);
            
            String newCode;
            do {
                newCode = referralCodeService.generateCode(savedProfile.getId());
            } while (profileRepository.existsByReferralCode(newCode));

            savedProfile.setReferralCode(newCode);
            savedProfile = profileRepository.save(savedProfile); // Save the profile again with referral code
            
            return new ProfileResponse(
                savedProfile.getId(),
                user.getId(),
                savedProfile.getName(),
                user.getEmail(),
                savedProfile.getUserType(),
                savedProfile.getReferralCode()
            );

        } else if (request.userType() == Profile.UserType.PARTNER) {
            if (request.referredByCode() == null || request.referredByCode().isBlank()) {
                throw new IllegalArgumentException("Referral code is required for partner use.");
            }
            if (!profileRepository.existsByReferralCode(request.referredByCode())) {
                throw new IllegalArgumentException("Invalid referral code: " + request.referredByCode());
            }
            profile.setNickName(request.nickname());
            profile.setReferredCode(request.referredByCode());
            
            Profile savedProfile = profileRepository.save(profile);
            
            return new ProfileResponse(
                savedProfile.getId(),
                user.getId(),
                savedProfile.getName(),
                user.getEmail(),
                savedProfile.getUserType(),
                savedProfile.getReferralCode()
            );
        }
        
        throw new IllegalStateException("Invalid usage type specified.");
    }

    public User loginUser(LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.email())
        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
        throw new IllegalArgumentException("Invalid email or password");
    }
    return user;
}

public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    @Transactional
    public ProfileServiceDto updateUserService(Long userId, ProfileServiceDto profileServiceDto) {
    // 1. Find the profile by user ID or throw an exception if not found
    Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

    UserServiceType newServiceType = profileServiceDto.getPreferredServiceType();
    profile.setPreferredServiceType(newServiceType);
    Profile updatedProfile = profileRepository.save(profile);
    return new ProfileServiceDto(updatedProfile.getPreferredServiceType());
}


    @Transactional
public MenstrualTrackingDto updateMenstrualData(Long userId, MenstrualTrackingDto updateDto) {
    Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

    // Store the previous values for comparison
    LocalDate previousLastPeriodStartDate = profile.getLastPeriodStartDate();
    Integer previousCycleLength = profile.getCycleLength();
    
    // Update the profile with new data
    profile.setLastPeriodStartDate(updateDto.getLastPeriodStartDate());
    profile.setLastPeriodEndDate(updateDto.getLastPeriodEndDate());
    profile.setPeriodLength(updateDto.getPeriodLength());
    profile.setCycleLength(updateDto.getCycleLength());
    
    profileRepository.save(profile);
    return updateDto;
    
}
    public CyclePredictionDto predictNextCycle(Long userId){
        Profile profile = profileRepository.findByUserId(userId)
               .orElseThrow(() -> new IllegalArgumentException("Profile not found for user Id : " + userId));
        
        LocalDate lastPeriodDate = profile.getLastPeriodStartDate();
        Integer cycleLength = profile.getCycleLength();
        Integer periodLength = profile.getPeriodLength();

        if(lastPeriodDate == null || cycleLength ==  null  || periodLength == null) {
            throw new IllegalArgumentException("Insufficient data to predict next cycle.");
        }

        LocalDate nextPeriodStartDate = lastPeriodDate.plusDays(cycleLength);
        LocalDate followingPeriodStartDate = nextPeriodStartDate.plusDays(cycleLength);
        LocalDate nextOvulationDate = followingPeriodStartDate.minusDays(14); 

        CyclePredictionDto prediction = new CyclePredictionDto();
        prediction.setNextPeriodStartDate(nextPeriodStartDate);
        prediction.setNextPeriodEndDate(nextPeriodStartDate.plusDays(periodLength - 1));
        prediction.setNextFollicularStartDate(nextPeriodStartDate);
        prediction.setNextFollicularEndDate(nextOvulationDate);
        prediction.setNextOvulationDate(nextOvulationDate);
        prediction.setNextOvulationEndDate(nextOvulationDate.plusDays(1));
        prediction.setNextLutealStartDate(nextOvulationDate.plusDays(1));
        prediction.setNextLutealEndDate(nextOvulationDate.plusDays(14));
        prediction.setNextFertileWindowStartDate(nextOvulationDate.minusDays(5));
        prediction.setNextFertileWindowEndDate(nextOvulationDate.plusDays(1));

        return prediction;
    }

    public String getCyclePredictionAsText(Long userId) {
        try {
            // 1. Get the structured prediction data
            CyclePredictionDto prediction = predictNextCycle(userId);
            
            // 2. Format the dates into a simple, clear format
            // Using a basic formatter here as the NLP service will handle localization.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

            // 3. Create the raw text string
            return String.format(
                "The user's next period is predicted to start on %s and end on %s. " +
                "Their most fertile window is predicted to be between %s and %s.",
                prediction.getNextPeriodStartDate().format(formatter),
                prediction.getNextPeriodEndDate().format(formatter),
                prediction.getNextFertileWindowStartDate().format(formatter),
                prediction.getNextFertileWindowEndDate().format(formatter)
            );
        } catch (Exception e) {
            // Handle cases where prediction is not possible
            return "The user has not provided enough information to generate a cycle prediction. Please ask them to complete their menstrual cycle setup.";
        }
    }

    public Profile findProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));
    }

    public Profile updateUserLanguage(Long userId, String languageCode) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        profile.setLanguageCode(languageCode);
        return profileRepository.save(profile);
    }

    public Profile updateBasicProfile(Long userId, Map<String, Object> updateData) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        // Update fields if they exist in the payload
        if (updateData.containsKey("age") && updateData.get("age") != null) {
            profile.setAge(((Number) updateData.get("age")).intValue());
        }
        if (updateData.containsKey("height") && updateData.get("height") != null) {
            profile.setHeight(((Number) updateData.get("height")).doubleValue());
        }
        if (updateData.containsKey("weight") && updateData.get("weight") != null) {
            profile.setWeight(((Number) updateData.get("weight")).doubleValue());
        }
        if (updateData.containsKey("name") && updateData.get("name") != null) {
            profile.setName((String) updateData.get("name"));
        }
        if (updateData.containsKey("nick_name") && updateData.get("nick_name") != null) {
            profile.setNickName((String) updateData.get("nick_name"));
        }

        return profileRepository.save(profile);
    }

    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    public PartnerDataDto getPartnerData(Long userId){
        Optional<Profile> partnerProfile = profileRepository.findByUserId(userId);

        if(partnerProfile.isEmpty() || partnerProfile.get().getUserType() != Profile.UserType.PARTNER) {
            throw new IllegalArgumentException("No partner profile found for user ID: " + userId);
        }

        String referralCode = partnerProfile.get().getReferredCode();
        if(referralCode == null || referralCode.isBlank()) {
            throw new IllegalArgumentException("Partner profile does not have a referral code.");
        }

        Profile userProfile = profileRepository.findByReferralCode(referralCode)
            .orElseThrow(() -> new IllegalArgumentException("No user profile found for referral code: " + referralCode));
        
        CyclePredictionDto cyclePrediction = predictNextCycle(userProfile.getUser().getId());

        return new PartnerDataDto(
            userProfile.getName(),
            cyclePrediction
        );
       
    }

    @Transactional
    public BreastCancerExamLog createBreastCancerExamLog(Long userId, Map<SymptomLocation, SymptomSide> symptoms) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        BreastCancerExamLog examLog = new BreastCancerExamLog();
        examLog.setProfile(profile);
        examLog.setExamDate(LocalDate.now());
        examLog.setCreatedAt(LocalDate.now());
        examLog.setSymptoms(symptoms);

        return selfExamLogRepository.save(examLog);
    }

    @Transactional
    public String processMcqRiskAssesment(Long userId, Map<String, String> answers) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        int score = calculateRiskScoreFromMcq(answers);
        String riskLevel;
        if(score >= 10){
            riskLevel = "High Risk";
        } else if(score >= 5) {
            riskLevel = "Moderate Risk";
        } else {
            riskLevel = "Low Risk";
        }
        profile.setRiskAssessmentMcqData(answers);
        profile.setBreastCancerRiskLevel(riskLevel);

        profileRepository.save(profile);
        return riskLevel;
    }

    private int calculateRiskScoreFromMcq(Map<String, String> answers) {
    int score = 0;

    // Menstrual & Reproductive History
    if ("MENSTRUATION_START_LT_12".equals(answers.get("menstruation_start_age"))) score += 1;
    if ("MENOPAUSE_YES_GT_55".equals(answers.get("menopause_status"))) score += 2;
    if ("PREGNANCY_NO".equals(answers.get("pregnancy_history"))) score += 1; // Nulliparity
    if ("BREASTFED_NO".equals(answers.get("breastfeeding_history"))) score += 1;
    if ("BREASTFED_NA".equals(answers.get("breastfeeding_history"))) score += 1; // Same as no
    if ("BREASTFED_YES_GT_6MO".equals(answers.get("breastfeeding_history"))) score -= 1; // Protective factor

    // Lifestyle & Hormonal Factors
    if ("ALCOHOL_WEEKLY".equals(answers.get("alcohol_use"))) score += 1;
    if ("ALCOHOL_DAILY".equals(answers.get("alcohol_use"))) score += 2;
    if ("SMOKING_OCCASIONALLY".equals(answers.get("smoking_status"))) score += 1;
    if ("SMOKING_REGULARLY".equals(answers.get("smoking_status"))) score += 2;
    if ("HRT_YES_LT_5Y".equals(answers.get("hrt_use"))) score += 1;
    if ("HRT_YES_GT_5Y".equals(answers.get("hrt_use"))) score += 2;
    if ("OC_YES_LT_5Y".equals(answers.get("oral_contraceptives_use"))) score += 1;
    if ("OC_YES_GT_5Y".equals(answers.get("oral_contraceptives_use"))) score += 2;

    // Also include the original high-impact factors
    if ("YES_FIRST_DEGREE".equals(answers.get("family_history"))) score += 5;
    if ("YES_ATYPICAL_HYPERPLASIA".equals(answers.get("personal_history_biopsy"))) score += 4;
    if ("AGE_50_PLUS".equals(answers.get("age_group"))) score += 4;


    return Math.max(0, score); // Ensure score doesn't go below 0
}
    // Debug helper to retrieve latest OTP for an email
    public String getLatestOtpForEmail(String email) {
        return otpGenerationService.getLatestOtp(email);
    }
}
