// package com.sheandsoul.v1update.services;

// import com.sheandsoul.v1update.dto.CyclePredictionDto;
// import com.sheandsoul.v1update.entities.Profile; 

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.time.format.DateTimeFormatter;

// /**
//  * This service acts as the "brain" for the AI assistant.
//  * It uses NLP to understand user intent and routes requests accordingly.
//  * It also personalizes responses by fetching and using the user's name.
//  */
// @Service
// public class NlpService {

//     @Autowired
//     private GeminiService geminiService;

//     @Autowired
//     private AppService appService; // Your existing service with business logic

//     public String processMessage(String userText, Long userId) {
//         Profile userProfile = appService.findProfileByUserId(userId);
//         String userName = userProfile.getNickName();

//         String intent = classifyIntent(userText);

//         switch (intent) {
//             case "GET_CYCLE_PREDICTION":
//                 return handleCyclePrediction(userId, userName);

//             // --- NEW: Route for the food and allergy intent ---
//             case "FOOD_ALLERGY":
//                 return handleFoodAndAllergy(userText, userName);

//             case "GENERAL_QUESTION":
//             default:
//                 return handleGeneralQuestion(userText, userName);
//         }
//     }

//     private String classifyIntent(String userText) {
//         // --- UPDATED: Added your new intent to the prompt ---
//         String prompt = "You are an intent classification system for a Menstrual health service app. " +
//                         "Analyze the user's message and classify it into one of the following intents: " +
//                         "'GET_CYCLE_PREDICTION', 'FOOD_ALLERGY', 'GENERAL_QUESTION'. " +
//                         "Respond with ONLY the intent name. For example, if the user asks 'what should I eat for cramps?', respond with 'FOOD_ALLERGY'.\n\n" +
//                         "User message: \"" + userText + "\"\n" +
//                         "Intent:";

//         String response = geminiService.getGeminiResponse(prompt);
        
//         if (response.toUpperCase().contains("GET_CYCLE_PREDICTION")) {
//             return "GET_CYCLE_PREDICTION";
//         }
//         // --- NEW: Check for the food and allergy intent ---
//         if (response.toUpperCase().contains("FOOD_ALLERGY")) {
//             return "FOOD_ALLERGY";
//         }
//         return "GENERAL_QUESTION";
//     }

//     // --- NEW: Handler method for food and allergy questions ---
//     private String handleFoodAndAllergy(String userText, String userName) {
//         String personalizedPrompt = String.format(
//             "You are a helpful health assistant specializing in nutrition and menstrual health. The user's name is %s. " +
//             "They are asking about food or allergies. Provide a safe, helpful, and supportive answer based on their question. " +
//             "If the question is about a serious medical condition or allergy, advise them to consult a doctor. \n\n" +
//             "User's question: \"%s\"",
//             userName, userText
//         );
//         return geminiService.getGeminiResponse(personalizedPrompt);
//     }

//     private String handleCyclePrediction(Long userId, String userName) {
//         try {
//             CyclePredictionDto prediction = appService.predictNextCycle(userId);
//             DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

//             return String.format(
//                 "Of course, %s! Based on your data, here is your next cycle prediction:\n\n" +
//                 "🔹 Next Period: Starts %s and ends around %s.\n" +
//                 "🔹 Most Fertile Window: Between %s and %s.",
//                 userName,
//                 prediction.getNextPeriodStartDate().format(formatter),
//                 prediction.getNextPeriodEndDate().format(formatter),
//                 prediction.getNextFertileWindowStartDate().format(formatter),
//                 prediction.getNextFertileWindowEndDate().format(formatter)
//             );
//         } catch (Exception e) {
//             return "It looks like you haven't set up your menstrual tracking yet, " + userName + ". " +
//                    "Once you provide your last period date and cycle length, I can give you a prediction!";
//         }
//     }

//     private String handleGeneralQuestion(String userText, String userName) {
//         String personalizedPrompt = String.format(
//             "You are a friendly and empathetic health assistant. The user's name is %s. " +
//             "Please be warm and address them by name if it feels natural. " +
//             "Provide a helpful and safe response to their question.\n\n" +
//             "User's question: \"%s\"",
//             userName, userText
//         );
//         return geminiService.getGeminiResponse(personalizedPrompt);
//     }
// }