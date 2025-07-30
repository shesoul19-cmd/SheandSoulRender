package com.sheandsoul.v1update.services;

import com.sheandsoul.v1update.entities.User;

public interface AssistantService {
    String getAssistantResponse(User user, String message);
}
