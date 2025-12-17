package com.finance.service;

import com.finance.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String frontendResetUrl; // e.g. https://app.example.com/reset-password
    private final String testRecipient;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.frontend.reset-password-url:http://localhost:3000/reset-password}") String frontendResetUrl,
                        @Value("${app.mail.test-recipient:}") String testRecipient) {
        this.mailSender = mailSender;
        this.frontendResetUrl = frontendResetUrl;
        this.testRecipient = testRecipient != null ? testRecipient.trim() : "";
        System.out.println("EmailService initialized with frontendResetUrl=" + this.frontendResetUrl + ", testRecipient=" + this.testRecipient);
    }

    public void sendPasswordResetEmail(User user, String token) {
        String recipient = (testRecipient != null && !testRecipient.isEmpty()) ? testRecipient : user.getEmail();
        String resetLink = frontendResetUrl + "?token=" + token;
        String subject = "Password reset request";
        String text = "Hi,\n\nTo reset your password click the link below:\n" + resetLink +
                "\n\nIf you didn't request this, ignore this email.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
