package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

import com.sheandsoul.v1update.entities.UserServiceType;

@Service
public class AssistantServiceFactory {

    private final MenstruationAssistantService menstruationAssistantService;
    private final BreastHealthAssistantService breastHealthAssistantService;
    private final MentalHealthAssistantService mentalHealthAssistantService;

    public AssistantServiceFactory(MenstruationAssistantService menstruationAssistantService, BreastHealthAssistantService breastHealthAssistantService, MentalHealthAssistantService mentalHealthAssistantService) {
        this.menstruationAssistantService = menstruationAssistantService;
        this.breastHealthAssistantService = breastHealthAssistantService;
        this.mentalHealthAssistantService = mentalHealthAssistantService;
    }

    public AssistantService getAssistant(UserServiceType serviceType) {
        if (serviceType == null) {
            return mentalHealthAssistantService;
        }
        switch (serviceType) {
            case MENSTRUAL:
                return menstruationAssistantService;
            case BREAST_CANCER:
                return breastHealthAssistantService;
            case MENTAL_HEALTH:
                return mentalHealthAssistantService;
            default:
                return mentalHealthAssistantService;
        }
    }
}
