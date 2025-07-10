package com.sheandsoul.v1update.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.AppService;
import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.dto.ProfileRequest;
import com.sheandsoul.v1update.dto.ProfileResponse;
import com.sheandsoul.v1update.dto.SignUpRequest;
import com.sheandsoul.v1update.dto.VerifyEmailRequest;
import com.sheandsoul.v1update.entities.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            User user = appService.registerUser(signUpRequest);
            // In a real app, you'd return a JWT here instead of the full user object.
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User registered successfully!",
                "userId", user.getId()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
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
    public ResponseEntity<?> resendOtp(@Valid @RequestBody VerifyEmailRequest request) {
        try{
            appService.resendOtp(request.email());
            return ResponseEntity.ok(Map.of("message", "OTP resent successfully!"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> setupProfile(@Valid @RequestBody ProfileRequest profileRequest) {
        try {
            ProfileResponse response = appService.createProfile(profileRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
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

}
