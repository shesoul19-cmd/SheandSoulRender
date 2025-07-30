package com.sheandsoul.v1update.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lowagie.text.DocumentException;
import com.sheandsoul.v1update.services.PCOSReportService;

import io.jsonwebtoken.io.IOException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final PCOSReportService pcosReportService;

    
    @GetMapping("/pcos/my-report")
    public ResponseEntity<byte[]> downloadMyPcosReport(Authentication authentication) throws Exception {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        try {
            // 2. Get the username (which is typically the email) from the principal
            String email = authentication.getName();

            // 3. Call the service to generate the PDF using the user's email
            byte[] pdfBytes = pcosReportService.generatePcosReportForUser(email);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            // Create a user-friendly filename
            String filename = "PCOS_Report_" + email.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (EntityNotFoundException e) {
            // This catches cases where the user, profile, or assessment doesn't exist.
            // Log the error for debugging purposes.
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (DocumentException | IOException e) {
            // This catches errors during PDF generation.
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/breast-cancer/my-report")
    public ResponseEntity<byte[]> downloadMyBreastCancerReport(Authentication authentication) throws Exception {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        try {
            String email = authentication.getName();
            byte[] pdfBytes = pcosReportService.generateBreastCancerReportForUser(email);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "Breast_Cancer_Report_" + email.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
