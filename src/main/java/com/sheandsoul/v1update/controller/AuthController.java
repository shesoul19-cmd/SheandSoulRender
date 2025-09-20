package com.sheandsoul.v1update.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sheandsoul.v1update.dto.AuthResponseDto;
import com.sheandsoul.v1update.dto.GoogleSignInRequest;
import com.sheandsoul.v1update.dto.LoginRequest;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.AppService;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.util.JwtUtil;

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

                // Use the service method to find or create the user
                User user = appService.findOrCreateUserForGoogleSignIn(email, name);

                // Generate your app's JWT token for the user
                final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                final String appJwt = jwtUtil.generateToken(userDetails);

                // Return the same response structure as your normal signup
                AuthResponseDto responseDto = new AuthResponseDto(
                    "Google Sign-In successful.",
                    user.getId(),
                    user.getEmail(),
                    appJwt
                );
                return ResponseEntity.ok(responseDto);

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google ID token.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during Google Sign-In.");
        }
    }

}
