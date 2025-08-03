package com.sheandsoul.v1update.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otp) {
        try {
            System.out.println("Attempting to send OTP email to: " + to + " with OTP: " + otp);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Set the "From" address to match your Zoho username
            helper.setFrom("support@sheandsoul.co.in");

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
            System.out.println("OTP email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send OTP email to " + to + ": " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to send OTP email: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error sending OTP email to " + to + ": " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Unexpected error sending OTP email: " + e.getMessage());
        }
    }
}
