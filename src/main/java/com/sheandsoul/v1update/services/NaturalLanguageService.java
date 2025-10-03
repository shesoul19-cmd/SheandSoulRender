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
        // return rawResponse.trim();
            // .replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<strong><em>$1</em></strong>")
            // .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
            // .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
            // .replaceAll("##\\s*(.*?)(?=\\n|$)", "<h2>$1</h2>")
            // .replaceAll("\\n", "<br/>");

        String formattedText = rawResponse.trim();

        // 1. Highlight/Bold: Replace **text** with <b>text</b>
        // The regex (.*?) is a non-greedy match for any characters inside the asterisks.
        formattedText = formattedText.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        
        // 2. Italics: Replace *text* with <i>text</i>
        // This regex uses a negative lookbehind (?<!\*) and lookahead (?!\*) to avoid matching **
        formattedText = formattedText.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)", "<i>$1</i>");

        // 3. Lists: Add a line break before list items for better spacing.
        // The (?m) flag enables multi-line mode for ^
        formattedText = formattedText.replaceAll("(?m)^[\\*\\-]\\s", "<br/>&bull; ");

        // 4. Newlines: Convert any remaining newline characters to <br> tags
        formattedText = formattedText.replaceAll("\\n", "<br/>");

        return formattedText;
    }
}
