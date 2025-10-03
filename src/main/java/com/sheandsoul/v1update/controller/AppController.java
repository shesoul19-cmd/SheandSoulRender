package com.sheandsoul.v1update.controller;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.AuthResponseDto;
import com.sheandsoul.v1update.dto.CyclePredictionDto;
import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.dto.MenstrualCycleLogDto;
import com.sheandsoul.v1update.dto.MenstrualTrackingDto;
import com.sheandsoul.v1update.dto.PartnerDataDto;
import com.sheandsoul.v1update.dto.ProfileRequest;
import com.sheandsoul.v1update.dto.ProfileResponse;
import com.sheandsoul.v1update.dto.ProfileServiceDto;
import com.sheandsoul.v1update.dto.ResendOtpRequest;
import com.sheandsoul.v1update.dto.SignUpRequest;
import com.sheandsoul.v1update.dto.VerifyEmailRequest;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.SymptomLocation;
import com.sheandsoul.v1update.entities.SymptomSide;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.AppService;
import com.sheandsoul.v1update.services.MenstruationAssistantService;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AppController {

     private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    private final AppService appService;
    private final MenstruationAssistantService menstruationAssistantService;
    private final MyUserDetailService userDetailsService;
    private final JwtUtil jwtUtil;

    public AppController(AppService appService, MenstruationAssistantService menstruationAssistantService, MyUserDetailService userDetailsService, JwtUtil jwtUtil) {
        this.appService = appService;
        this.menstruationAssistantService = menstruationAssistantService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }
    @GetMapping("/partner")
    public ResponseEntity<?> getPartnerData(Authentication authentication) {
       try {
            // 1. Identify the logged-in partner from their JWT
            User partnerUser = userDetailsService.findUserByEmail(authentication.getName());
            
            // 2. Call the service to fetch the linked user's data
            PartnerDataDto partnerData = appService.getPartnerData(partnerUser.getId());
            
            // 3. Return the data
            return ResponseEntity.ok(partnerData);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/next-period")
    public ResponseEntity<?> getNextPeriod(Authentication authentication) {
        try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            CyclePredictionDto prediction = appService.predictNextCycle(currentUser.getId());
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            User user = appService.registerUser(signUpRequest);
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

            final String jwt = jwtUtil.generateToken(userDetails);
            // In a real app, you'd return a JWT here instead of the full user object.
            // Return structured DTO aligning with Android client expectations
            AuthResponseDto responseDto = new AuthResponseDto(
                "User registered successfully! Please check your email for an OTP.",
                user.getId(),
                user.getEmail(),
                jwt,
                null, // name is not set yet
                null, // nickname is not set yet
                false // isProfileComplete is false for new users
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    @GetMapping("/profile/status")
    public ResponseEntity<?> getProfileStatus(Authentication authentication) {
        try {
            User user = userDetailsService.findUserByEmail(authentication.getName());
            Profile profile = user.getProfile();
            
            // A profile is "complete" if it exists and key onboarding fields are filled.
            boolean isComplete = profile != null && profile.getAge() != null && profile.getLastPeriodStartDate() != null;
            
            String name = (profile != null) ? profile.getName() : user.getEmail();
            String nickname = (profile != null) ? profile.getNickName() : "";
            
            // Using the AuthResponseDto for a consistent response structure.
            AuthResponseDto responseDto = new AuthResponseDto(
                "Profile status retrieved.",
                user.getId(),
                user.getEmail(),
                null, // No new token is issued here
                name,
                nickname,
                isComplete
            );
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request){
        try{
            appService.verifyEmail(request.email(), request.otp());
            return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
        }catch(IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        try{
            appService.resendOtp(request.email());
            return ResponseEntity.ok(Map.of("message", "OTP resent successfully!"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> setupProfile(@Valid @RequestBody ProfileRequest profileRequest, Authentication authentication) {
        logger.info("Received profile creation request: {}", profileRequest.toString());
        try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            ProfileResponse response = appService.createOrUpdateProfile(profileRequest, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Log the full stack trace for debugging
           logger.error("Profile creation failed with an unexpected exception!", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Profile creation failed: " + e.getMessage()));
        }
    }

    @PutMapping("/services")
    public ResponseEntity<?> updateUserServices(
        @Valid @RequestBody ProfileServiceDto updateServicesDto, 
        Authentication authentication) {
    try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            appService.updateUserService(currentUser.getId(), updateServicesDto);
            return ResponseEntity.ok(Map.of("message", "Services updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile/device-token")
    public ResponseEntity<?> updateDeviceToken(@RequestBody Map<String, String> payload, Authentication authentication) {
        String token = payload.get("deviceToken");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "deviceToken is required."));
        }
        try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            appService.updateUserDeviceToken(currentUser.getId(), token);
            return ResponseEntity.ok(Map.of("message", "Device token updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }


   // ... inside AppController class

@PutMapping("/menstrual-data")
public ResponseEntity<?> updateMenstrualEntity(
    @Valid @RequestBody MenstrualTrackingDto menstrualDataDto,
    Authentication authentication
) {
    User currentUser = userDetailsService.findUserByEmail(authentication.getName());
    appService.updateMenstrualData(currentUser.getId(), menstrualDataDto);
    return ResponseEntity.ok(Map.of("message", "Menstrual data updated successfully!"));
}

    @PutMapping("/language")
    public ResponseEntity<?> setUserLanguage(@RequestBody Map<String, String> payload, Authentication authentication) {
        String languageCode = payload.get("languageCode");
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "languageCode field is required."));
        }
        User currentUser = userDetailsService.findUserByEmail(authentication.getName());
        appService.updateUserLanguage(currentUser.getId(), languageCode);
        return ResponseEntity.ok(Map.of("message", "User language updated successfully to " + languageCode));
    }

    @PutMapping("/profile/basic")
    public ResponseEntity<?> updateBasicProfile(@RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            appService.updateBasicProfile(currentUser.getId(), payload);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/login")
public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
        User user = appService.loginUser(loginRequest);
        return ResponseEntity.ok(Map.of(
            "message", "Login successful!",
            "userId", user.getId(),
            "email", user.getEmail()
        ));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
    }
}

   @PostMapping("/breast-health")
    public ResponseEntity<?> logBreastCancerSelfExam(@RequestBody Map<String, String> rawSymptoms, Authentication authentication) {
        try {
            Map<SymptomLocation, SymptomSide> symptoms = rawSymptoms.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> SymptomLocation.valueOf(entry.getKey().toUpperCase()),
                    entry -> SymptomSide.valueOf(entry.getValue().toUpperCase())
                ));
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            appService.createBreastCancerExamLog(currentUser.getId(), symptoms);
            return ResponseEntity.ok(Map.of("message", "Detailed self-exam log saved successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid symptom location or side provided."));
        }
    }

    @PostMapping("/mcq-assessment")
    public ResponseEntity<?> submitMcqRiskAssessment(@RequestBody Map<String, String> answers, Authentication authentication) {
        try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            String riskLevel = appService.processMcqRiskAssesment(currentUser.getId(), answers);
            
            return ResponseEntity.ok(Map.of(
                "message", "Assessment completed successfully.",
                "riskLevel", riskLevel
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/menstrual-assistant")
    public ResponseEntity<?> menstrualAssistantRequest(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            String userMessage = request.get("message");
            
            // The service method only needs the user and the message.
            String response = menstruationAssistantService.getAssistantResponse(currentUser, userMessage);
            
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
     @GetMapping("/menstrual-logs")
    public ResponseEntity<List<MenstrualCycleLogDto>> getMenstrualLogs(Authentication authentication) {
        try {
            User currentUser = userDetailsService.findUserByEmail(authentication.getName());
            List<MenstrualCycleLogDto> history = appService.getMenstrualLogHistory(currentUser.getId());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // Or return a proper error message
        }
    }

    // DEBUG: Retrieve latest OTP for a given email
    @GetMapping("/debug/latest-otp")
    public ResponseEntity<?> getLatestOtp(@RequestParam String email) {
        try {
            String otp = appService.getLatestOtpForEmail(email);
            return ResponseEntity.ok(Map.of("email", email, "otp", otp));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}