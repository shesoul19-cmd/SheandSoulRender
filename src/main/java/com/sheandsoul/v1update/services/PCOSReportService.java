package com.sheandsoul.v1update.services;

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
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PCOSReportService {

    private final ProfileRepository profileRepository;
    private final PCOSAssessmentRepository pcosAssesmentRepository;
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

    private byte[] generatePcosReportPdf(Long profileId) throws DocumentException, IOException {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + profileId));

        PCOSAssesment assessment = pcosAssesmentRepository.findTopByProfileIdOrderByAssessmentDateDesc(profileId)
                .orElseThrow(() -> new EntityNotFoundException("PCOS Assessment not found for profile id: " + profileId));

        String prompt = buildPcosPrompt(profile, assessment);
        // Step 1: Get the raw response from the AI service
        String rawAiResponse = geminiService.getGeminiResponse(profile.getUser(), prompt);

        // Step 2: Format the raw response into safe, valid HTML
        String formattedAiResponse = formatAiResponseForHtml(rawAiResponse);

        Context context = new Context();
        context.setVariable("profile", profile);
        context.setVariable("assessment", assessment);
        // Step 3: Add the CORRECTLY formatted variable to the context
        context.setVariable("formattedAiResponse", formattedAiResponse);

        String htmlContent = templateEngine.process("reports/pcos-report", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream, false);
        renderer.finishPDF();

        return outputStream.toByteArray();
    }

    private String buildPcosPrompt(Profile profile, PCOSAssesment assessment) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a detailed health report for a user with potential PCOS symptoms. ");
        prompt.append("User Profile: Age - ").append(profile.getAge()).append(". ");
        // Ensure riskLevel and symptoms are not null before accessing
        if (assessment.getRiskLevel() != null) {
            prompt.append("PCOS Assessment Details: Risk Level - ").append(assessment.getRiskLevel()).append(". ");
        }
        if (assessment.getSymptoms() != null) {
            prompt.append("Symptoms: ").append(assessment.getSymptoms().toString()).append(". ");
        }
        prompt.append("Based on this data, provide a comprehensive analysis, potential health risks, and lifestyle recommendations. Use markdown for formatting: '***' for highlight-3, '**' for highlight-2, '*' for highlight-1, and '##' for headings. Make it personal and empathetic.");
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
