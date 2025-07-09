package com.sheandsoul.v1update;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.dto.ProfileRequest;
import com.sheandsoul.v1update.dto.ProfileResponse;
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

    public AppService(UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder, ReferralCodeService referralCodeService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.referralCodeService = referralCodeService;
    }

    @Transactional
    public User registerUser(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email already in use");
        }
        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        return userRepository.save(newUser);
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


}
