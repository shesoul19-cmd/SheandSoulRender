package com.sheandsoul.v1update.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.sheandsoul.v1update.entities.ChatMessage;
import com.sheandsoul.v1update.services.NlpService;

@Controller
public class ChatController {

    @Autowired
    private NlpService nlpService;

    @MessageMapping("/chat.sendMessage") // Listens for messages from /app/chat.sendMessage
    @SendTo("/topic/public") // Broadcasts the return value to all subscribers of /topic/public
    public ChatMessage sendMessage(ChatMessage chatMessage) throws Exception {
        // Show typing indicator logic would go here if desired
        Long userId = 1L; // Replace with actual user ID logic, e.g., from session or security context
        // Get the AI's response
        String aiResponseText = nlpService.processMessage(chatMessage.getText(), userId);
        
        // Return the AI's response message
        return new ChatMessage("AI", aiResponseText);
    }

}
