package com.sheandsoul.v1update.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sheandsoul.v1update.dto.ForgotPasswordRequest;
import com.sheandsoul.v1update.dto.GoogleSignInRequest;
import com.sheandsoul.v1update.dto.GoogleSignInResult;
import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.dto.ResetPasswordRequest;
import com.sheandsoul.v1update.dto.UserProfileDto;
import com.sheandsoul.v1update.dto.VerifyEmailRequest;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.AppService;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.util.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AppService appService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private MyUserDetailService userDetailsService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
        } catch (BadCredentialsException e) {
            // It's better to return a proper error response
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("message", "Incorrect username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());
        final User user = userDetailsService.findUserByEmail(loginRequest.email());
        final String jwt = jwtUtil.generateToken(userDetails);

        // ✅ START FIX: Create a response body that matches the Android client's expectations
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Login successful!");
        responseBody.put("user_id", user.getId());
        responseBody.put("email", user.getEmail());
        responseBody.put("access_token", jwt); // Use "access_token" as the key
        responseBody.put("token_type", "bearer");

        // Add user's name and nickname from their profile
        if (user.getProfile() != null) {
            responseBody.put("name", user.getProfile().getName());
            responseBody.put("nickname", user.getProfile().getNickName());
        } else {
            // Fallback if the profile hasn't been created yet
            responseBody.put("name", user.getEmail()); // Use email as a fallback
            responseBody.put("nickname", "");
        }
        
        return ResponseEntity.ok(responseBody);
        // ✅ END FIX
    }

    @PostMapping("/google")
    public ResponseEntity<?> handleGoogleSignIn(@Valid @RequestBody GoogleSignInRequest signInRequest) {
        // For now, we are not validating the google client id, but this should be added for production security.
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .build();

        
        try {
            GoogleIdToken idToken = verifier.verify(signInRequest.idToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                GoogleSignInResult result = appService.findOrCreateUserForGoogleSignIn(email, name);
                User user = result.user();

                final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                final String appJwt = jwtUtil.generateToken(userDetails);

                // ✅ BUILD A RESPONSE LIKE THE MAIN LOGIN ENDPOINT
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("message", "Google Sign-In successful!");
                responseBody.put("user_id", user.getId());
                responseBody.put("email", user.getEmail());
                responseBody.put("access_token", appJwt);
                responseBody.put("token_type", "bearer");
                responseBody.put("name", user.getProfile().getName());
                responseBody.put("nickname", user.getProfile().getNickName());
                // This is the crucial flag for the client!
                responseBody.put("is_profile_complete", !result.isNewUser());

                return ResponseEntity.ok(responseBody);

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Google ID token."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred."));
        }
    }
    

    // ... inside the AuthController class

@PostMapping("/password/forgot")
public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    try {
        appService.sendPasswordResetOtp(request.email());
        return ResponseEntity.ok(Map.of("message", "A password reset OTP has been sent to your email."));
    } catch (IllegalArgumentException e) {
        // Return a generic message to prevent attackers from guessing registered emails
        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a reset OTP has been sent."));
    }
}

@PostMapping("/password/verify-otp")
public ResponseEntity<?> verifyPasswordOtp(@Valid @RequestBody VerifyEmailRequest request) {
    try {
        boolean isValid = appService.verifyPasswordResetOtp(request.email(), request.otp());
        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "OTP is valid. You can now reset your password."));
        } else {
            // This case is handled by the exception below, but included for clarity
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid or expired OTP."));
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}

@PostMapping("/password/reset")
public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    try {
        appService.resetUserPassword(request.email(), request.otp(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Your password has been reset successfully."));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}

@GetMapping("/profile/me")
public ResponseEntity<?> getMyProfile(Authentication authentication) {
    try {
        String email = authentication.getName();
        UserProfileDto userProfileDto = appService.getUserProfile(email);
        return ResponseEntity.ok(userProfileDto);
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}

}
