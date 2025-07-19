package com.sheandsoul.v1update.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.sheandsoul.v1update.entities.ChatMessage;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.services.NlpService;

@Controller
public class ChatController {

    @Autowired
    private NlpService nlpService;

    @Autowired
    private MyUserDetailService myUserDetailsService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        
        // 1. Securely get the user's identity from the WebSocket session
        Authentication authentication = (Authentication) headerAccessor.getUser();

        if (authentication == null) {
            // This case should ideally not be reached if security is configured correctly
            return new ChatMessage("AI", "Error: You are not authenticated.");
        }

        // 2. Get the user's email and find their full profile to get the ID
        String userEmail = authentication.getName();
        User currentUser = myUserDetailsService.findUserByEmail(userEmail);
        Long userId = currentUser.getId();

        // 3. Process the message using the dynamic, authenticated user ID
        String aiResponseText = nlpService.processMessage(chatMessage.getText(), userId);
        
        // 4. Return the AI's response message
        return new ChatMessage("AI", aiResponseText);
    }
}
