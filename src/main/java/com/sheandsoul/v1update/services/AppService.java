package com.sheandsoul.v1update.services;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sheandsoul.v1update.dto.CyclePredictionDto;
import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.dto.MenstrualTrackingDto;
import com.sheandsoul.v1update.dto.ProfileRequest;
import com.sheandsoul.v1update.dto.ProfileResponse;
import com.sheandsoul.v1update.dto.ProfileServiceDto;
import com.sheandsoul.v1update.dto.SignUpRequest;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;
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

    public AppService(UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder, ReferralCodeService referralCodeService, OtpGenerationService otpGenerationService, EmailService emailService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.referralCodeService = referralCodeService;
        this.otpGenerationService = otpGenerationService;
        this.emailService = emailService;
    }

    @Transactional
    public User registerUser(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email already in use");
        }
        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setEmailVerified(false);

        User savedUser = userRepository.save(newUser);

        String otp = otpGenerationService.generateOtp();
        otpGenerationService.storeOtp(savedUser.getEmail(), otp);
        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        return savedUser;
        
    }

    @Transactional
    public void verifyEmail(String email, String submittedOtp){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
        if(user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified.");
        }

        String cachedOtp = otpGenerationService.getOtp(email);
        if (cachedOtp == null || !cachedOtp.equals(submittedOtp)) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        if(otpGenerationService.isOtpExpired(email)) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        otpGenerationService.clearOtp(email);
    }

    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified.");
        }

        String otp = otpGenerationService.generateOtp();
        otpGenerationService.storeOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.userId()));

        if (user.getProfile() != null) {
            throw new IllegalStateException("User already has a profile.");
        }

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setName(request.name());
        profile.setNickName(request.nickname());
        profile.setUserType(request.userType());

        if (request.userType() == Profile.UserType.USER) {
            // Logic for self-use user
            profile.setAge(request.age());
            profile.setHeight(request.height());
            profile.setWeight(request.weight());
            
            // **FIX**: Use saveAndFlush to save the profile and guarantee the ID is generated immediately.
            Profile savedProfile = profileRepository.saveAndFlush(profile);
            
            // Now savedProfile.getId() is guaranteed to be non-null.
            String newCode;
            do {
                newCode = referralCodeService.generateCode(savedProfile.getId());
            } while (profileRepository.existsByReferralCode(newCode));

            // Set the code. JPA will handle the final update when the transaction commits.
            savedProfile.setReferralCode(newCode);
            
            return new ProfileResponse(
                savedProfile.getId(),
                user.getId(),
                savedProfile.getName(),
                user.getEmail(),
                savedProfile.getUserType(),
                savedProfile.getReferralCode()
            );

        } else if (request.userType() == Profile.UserType.PARTNER) {
            // Logic for partner-use user
            if (request.referredByCode() == null || request.referredByCode().isBlank()) {
                throw new IllegalArgumentException("Referral code is required for partner use.");
            }
            if (!profileRepository.existsByReferralCode(request.referredByCode())) {
                throw new IllegalArgumentException("Invalid referral code: " + request.referredByCode());
            }
            profile.setReferredCode(request.referredByCode());
            
            Profile savedProfile = profileRepository.save(profile);
            
            return new ProfileResponse(
                savedProfile.getId(),
                user.getId(),
                savedProfile.getName(),
                user.getEmail(),
                savedProfile.getUserType(),
                savedProfile.getReferralCode() // This will be null for partner users
            );
        }
        
        // This line should not be reachable if usageType is always valid.
        throw new IllegalStateException("Invalid usage type specified.");
    }

    public User loginUser(LoginRequest loginRequest) {
    // Step 1: Find user by email
    User user = userRepository.findByEmail(loginRequest.email())
        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    // Step 2: Check password (assuming you stored it using BCrypt)
    if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
        throw new IllegalArgumentException("Invalid email or password");
    }

    // Step 3: Return user if valid
    return user;
}

public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    public ProfileServiceDto updateUserService(Long userId, ProfileServiceDto profileServiceDto) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        profile.setEnableMenstrualService(profileServiceDto.isMenstrualServiceEnabled());
        profile.setEnableBreastCancerService(profileServiceDto.isBreastCancerServiceEnabled());

        Profile savedProfile = profileRepository.save(profile);
        return profileServiceDto;
    }

    @Transactional
public MenstrualTrackingDto updateMenstrualData(Long userId, MenstrualTrackingDto updateDto) {
    Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

    profile.setLastPeriodStartDate(updateDto.getLastPeriodStartDate());
    profile.setLastPeriodEndDate(updateDto.getLastPeriodEndDate());
    profile.setPeriodLength(updateDto.getPeriodLength());
    profile.setCycleLength(updateDto.getCycleLength());
    Profile savedProfile =  profileRepository.save(profile);
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
    
}
