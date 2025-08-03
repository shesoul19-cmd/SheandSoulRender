package com.sheandsoul.v1update.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otp) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            logger.info("Attempting to send OTP email. To: {}, OTP: {}", to, otp);
            
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Set the "From" address to match your Zoho username
            helper.setFrom("support@sheandsoul.co.in", "She&Soul");

            String emailContent = "<h3>Hello,</h3>"
                                + "<p>Thank you for signing up. Please use the following One-Time Password (OTP) to verify your email address:</p>"
                                + "<h2>" + otp + "</h2>"
                                + "<p>This OTP is valid for 10 minutes. If you did not request this, please ignore this email.</p>"
                                + "<br>"
                                + "<p>Best regards,<br>She&Soul Team</p>";

            helper.setText(emailContent, true); // true indicates HTML content
            helper.setTo(to);
            helper.setSubject("Your Email Verification Code");

            mailSender.send(mimeMessage);
            logger.info("OTP email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("Failed to send OTP email due to MessagingException.");
        } catch (Exception e) {
            logger.error("An unexpected error occurred while sending OTP email to {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("Unexpected error sending OTP email.");
        }
    }
}
