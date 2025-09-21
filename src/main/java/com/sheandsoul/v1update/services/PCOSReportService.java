package com.sheandsoul.v1update.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;
import com.sheandsoul.v1update.entities.BreastCancerExamLog;
import com.sheandsoul.v1update.entities.PCOSAssesment;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.repository.BreastCancerSelfExamLogRepository;
import com.sheandsoul.v1update.repository.PCOSAssessmentRepository;
import com.sheandsoul.v1update.repository.ProfileRepository;
import com.sheandsoul.v1update.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PCOSReportService {

    private final PCOSAssessmentRepository pcosAssessmentRepository; // Corrected variable name
    private final ProfileRepository profileRepository;
    private final AppService appService;
    private final BreastCancerSelfExamLogRepository breastCancerSelfExamLogRepository;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public byte[] generatePcosReportForUser(String email) throws DocumentException, IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        Profile profile = user.getProfile();
        if (profile == null) {
            throw new EntityNotFoundException("Profile not found for user with email: " + email);
        }
        return generatePcosReportPdf(profile.getId());
    }

    // Inside PCOSReportService.java

private byte[] generatePcosReportPdf(Long profileId) throws DocumentException, IOException {
    Profile profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + profileId));

    PCOSAssesment assessment = pcosAssessmentRepository.findTopByProfileIdOrderByAssessmentDateDesc(profileId)
            .orElseThrow(() -> new EntityNotFoundException("PCOS Assessment not found for profile id: " + profileId));

    String prompt = buildPcosPrompt(profile, assessment);
    
    // FIX: Call getGeminiResponse with only the prompt string
    String rawAiResponse = geminiService.getGeminiResponse(prompt); 
    
    // This sanitization step is now crucial
    String formattedAiResponse = formatAiResponseForHtml(rawAiResponse);

    Context context = new Context();
    context.setVariable("profile", profile);
    context.setVariable("assessment", assessment);
    context.setVariable("formattedAiResponse", formattedAiResponse); // Pass the sanitized HTML

    String htmlContent = templateEngine.process("reports/pcos-report", context);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(htmlContent);
    renderer.layout();
    renderer.createPDF(outputStream, false); // changed to false, true is for finishing
    renderer.finishPDF();

    return outputStream.toByteArray();
}

    // FIX 2: This method is completely updated to build a prompt with the new data.
    private String buildPcosPrompt(Profile profile, PCOSAssesment assessment) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a detailed, empathetic, and professional health report for a user with potential PCOS symptoms. ");
        prompt.append("The user's name is ").append(profile.getName()).append(" and their age is ").append(profile.getAge()).append(". ");
        prompt.append("Their calculated risk level is '").append(assessment.getRiskLevel()).append("'. ");
        prompt.append("Here are their answers to the screening quiz: ");
        prompt.append("- Irregular periods? ").append(assessment.getCycleLengthDays() > 35 || assessment.getMissedPeriodsInLastYear() >= 4 ? "Yes" : "No").append(". ");
        prompt.append("- Severe acne? ").append(assessment.getHasSevereAcne() ? "Yes" : "No").append(". ");
        prompt.append("- Excess hair growth? ").append(assessment.getHasExcessHairGrowth() ? "Yes" : "No").append(". ");
        prompt.append("- Thinning hair? ").append(assessment.getHasThinningHair() ? "Yes" : "No").append(". ");
        prompt.append("- Doctor-confirmed ovarian cysts? ").append(assessment.getHasOvarianCystsConfirmedByUltrasound() ? "Yes" : "No").append(". ");
        prompt.append("- Weight gain issues? ").append(assessment.getHasWeightGainOrObesity() ? "Yes" : "No").append(". ");
        prompt.append("- Dark skin patches? ").append(assessment.getHasDarkSkinPatches() ? "Yes" : "No").append(". ");
        prompt.append("- Family history of PCOS? ").append(assessment.getHasFamilyHistoryOfPCOS() ? "Yes" : "No").append(". ");
        prompt.append("Based on this data, provide a comprehensive analysis covering what their risk level means, which symptoms are most indicative, potential health implications, and actionable lifestyle recommendations for diet, exercise, and stress management. ");
        prompt.append("The tone should be supportive, not alarming. Use markdown for formatting: '***' for important highlights, '**' for bolded subheadings, '*' for italics, and '##' for main section headings. End with a clear disclaimer to consult a healthcare professional.");
        return prompt.toString();
    }

    public byte[] generateBreastCancerReportForUser(String email) throws DocumentException, IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        Profile profile = user.getProfile();
        if (profile == null) {
            throw new EntityNotFoundException("Profile not found for user with email: " + email);
        }
        return generateBreastCancerReportPdf(profile.getId());
    }

    private byte[] generateBreastCancerReportPdf(Long profileId) throws DocumentException, IOException {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + profileId));

        BreastCancerExamLog examLog = breastCancerSelfExamLogRepository.findTopByProfileIdOrderByExamDateDesc(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Breast Cancer Exam Log not found for profile id: " + profileId));

        String prompt = buildBreastCancerPrompt(profile, examLog);
        String rawAiResponse = geminiService.getGeminiResponse(profile.getUser(), prompt);
        String formattedAiResponse = formatAiResponseForHtml(rawAiResponse);


        Context context = new Context();
        context.setVariable("profile", profile);
        context.setVariable("examLog", examLog);
        context.setVariable("formattedAiResponse", formattedAiResponse);

        String htmlContent = templateEngine.process("reports/breast-cancer-report", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream, false);
        renderer.finishPDF();

        return outputStream.toByteArray();
    }

    /**
     * Formats a raw string from an AI service into a safe HTML string.
     * It replaces markdown-like syntax with proper, balanced HTML tags.
     * The order of replacement is important: from most specific (***) to least specific (*).
     */
    public String formatAiResponseForHtml(String aiResponse) {
        if (aiResponse == null) {
            return "";
        }
        return aiResponse
            // Replace ***text*** with <span class="highlight-3">text</span>
            .replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<span class=\"highlight-3\">$1</span>")
            // Replace **text** with <span class="highlight-2">text</span>
            .replaceAll("\\*\\*(.*?)\\*\\*", "<span class=\"highlight-2\">$1</span>")
            // Replace *text* with <span class="highlight-1">text</span>
            .replaceAll("\\*(.*?)\\*", "<span class=\"highlight-1\">$1</span>")
            // Replace ## text followed by a newline with <h2>text</h2>
            .replaceAll("##\\s*(.*?)(?=\\n|$)", "<h2>$1</h2>")
            // Replace any remaining newline characters with <br/> tags
            .replaceAll("\\n", "<br/>");
    }

    private String buildBreastCancerPrompt(Profile profile, BreastCancerExamLog examLog) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a breast health report based on a self-examination log. ");
        prompt.append("User Profile: Age - ").append(profile.getAge()).append(". ");
        prompt.append("Exam Log Details: Exam Date - ").append(examLog.getExamDate()).append(". ");
        if (examLog.getSymptoms() != null) {
            prompt.append("Symptoms: ").append(examLog.getSymptoms().toString()).append(". ");
        }
        prompt.append("Based on this data, provide a summary of the aifindings, information about breast health, and when to consult a doctor. Use markdown for formatting. The tone should be supportive and informative.");
        return prompt.toString();
    }
}
