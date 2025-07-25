package com.sheandsoul.v1update.services;
import com.lowagie.text.DocumentException;
import com.sheandsoul.v1update.entities.PCOSAssesment;
import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;
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
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;

    public byte[] generatePcosReportForUser(String email) throws DocumentException, IOException {
        // Find the User entity by email first
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        
        Profile profile = user.getProfile();
        if (profile == null) {
            throw new EntityNotFoundException("Profile not found for user with email: " + email);
        }
        return generatePcosReportPdf(profile.getId());
    }

    private byte[] generatePcosReportPdf(Long profileId) throws DocumentException, IOException {
        // 1. Fetch data
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + profileId));
        
        PCOSAssesment assessment = pcosAssesmentRepository.findTopByProfileIdOrderByAssessmentDateDesc(profileId)
                .orElseThrow(() -> new EntityNotFoundException("PCOS Assessment not found for profile id: " + profileId));

        // 2. Prepare data for the template
        Context context = new Context();
        context.setVariable("profile", profile);
        context.setVariable("assessment", assessment);

        // 3. Process the HTML template with data
        String htmlContent = templateEngine.process("reports/pcos-report", context);

        // 4. Generate PDF from HTML
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream, false);
        renderer.finishPDF();

        return outputStream.toByteArray();
    }

}
