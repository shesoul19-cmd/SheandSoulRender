package com.sheandsoul.v1update.services;

import org.springframework.stereotype.Service;

@Service
public class NaturalLanguageService {

    public String formatResponse(String rawResponse) {
        if (rawResponse == null) {
            return "";
        }
        // This is a simple implementation that replaces markdown with HTML.
        // More complex NLP tasks can be added here in the future.
        return rawResponse
            .replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<strong><em>$1</em></strong>")
            .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
            .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
            .replaceAll("##\\s*(.*?)(?=\\n|$)", "<h2>$1</h2>")
            .replaceAll("\\n", "<br/>");
    }
}
