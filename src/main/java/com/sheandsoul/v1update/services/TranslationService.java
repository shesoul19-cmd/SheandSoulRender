package com.sheandsoul.v1update.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    

    @Autowired
    private GeminiService geminiService;

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String translate(String text, String targetLanguageCode){
        if ("en".equalsIgnoreCase(targetLanguageCode) || text == null || text.trim().isEmpty()) {
            return text;
        }

        String targetLanguageName = convertCodeToName(targetLanguageCode);
        String cacheKey = String.format("en:%s:%s", targetLanguageCode, text);
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        String prompt = String.format(
            "You are a professional translation engine. Translate the following English text to %s. " +
            "IMPORTANT: Respond with ONLY the translated text and nothing else.\n\n" +
            "Text to translate: \"%s\"",
            targetLanguageName, text
        );

        String translation = geminiService.getGeminiResponse(prompt).trim();
        cache.put(cacheKey, translation);
        return translation;
    }

     private String convertCodeToName(String code) {
        switch (code.toLowerCase()) {
            case "hi": return "Hindi";
            case "bn": return "Bengali";
            case "ta": return "Tamil";
            case "te": return "Telugu";
            case "mr": return "Marathi";
            case "gu": return "Gujarati";
            case "kn": return "Kannada";
            case "ml": return "Malayalam";
            case "pa": return "Punjabi";
            case "ur": return "Urdu";
            case "nep" : return "Nepali";
            case "mn": return "Manipuri";
            case "or": return "Odia";
            case "as": return "Assamese";
            case "mai": return "Maithili";
            case "san": return "Sanskrit";
            case "sa": return "Santali";
            case "ko": return "Konkani";
            case "ka": return "Kashmiri";
            case "bh": return "Bhojpuri";
            case "bd": return "Bodo";
            case "do": return "Dogri";

            case "es": return "Spanish";
            case "fr": return "French";
            case "de": return "German";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            case "zh": return "Chinese";
            case "ja": return "Japanese";
            case "ar": return "Arabic";
            case "ru": return "Russian";
            case "vi": return "Vietnamese";
            case "th": return "Thai";
            case "id": return "Indonesian";
            case "tr": return "Turkish";
            case "en": return "English"; // Default case for English
            default: return "English";
        }
    }

}
