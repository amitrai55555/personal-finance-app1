package com.finance.service.Mail;

import com.finance.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the post-registration welcome email.
 */
@Service
public class WelcomeEmailService {

    private final JavaMailSender mailSender;
    private final String testRecipient;
    private final String fromAddress;

    public WelcomeEmailService(JavaMailSender mailSender,
                               @Value("${app.mail.test-recipient:}") String testRecipient,
                               @Value("${spring.mail.username:FinTrackrTech@gmail.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.testRecipient = testRecipient != null ? testRecipient.trim() : "";
        this.fromAddress = fromAddress;
    }

    public void sendWelcomeEmail(User user) {
        String recipient = (!testRecipient.isEmpty()) ? testRecipient : user.getEmail();

        String subject = "Welcome to FinTrackr";
        String text = """
                Hi %s,

                Thank you for choosing FinTrackr. We’re excited to welcome you.

                FinTrackr helps you track income, expenses, investments, and download detailed reports—all designed to give you clarity and control over your finances.

                Start exploring your dashboard and take the first step toward smarter financial decisions.

                Welcome aboard,

                Team FinTrackr
                """.formatted(user.getFirstName());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
