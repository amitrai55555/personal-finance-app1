package com.finance.service.Mail;

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
        String text =   "Hi " + user.getFirstName() + ",Greetings from FinTrackr!\n" +
                "\n" +
                "We received a request to reset the password for your FinTrackr account.\n" +
                "\n" +
                "To proceed, please click the link below and follow the instructions to set a new password:\n" +
                "\n" +
                "Reset Password\n" +resetLink+
                "\n" +
                "For your security, this link is valid for a limited time. If you did not request a password reset, please ignore this email—your account will remain secure.\n" +
                "\n" +
                "If you need any assistance, our support team is always here to help.\n" +
                "\n" +
                "Stay secure and in control of your finances,\n" +
                "Team FinTrackr";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    public void sendWelcomeEmail(User user) {

        String recipient =
                (testRecipient != null && !testRecipient.isEmpty())
                        ? testRecipient
                        : user.getEmail();

        String subject = "Welcome to Finance App 🎉";
        String text =
                "Hi " + user.getFirstName() + ",\n\n" +
                        "Thank you for choosing FinTrackr. We’re excited to welcome you as the newest member of our growing FinTrackr community.\n\n" +
                        "You’ve just made a smart move by choosing FinTrackr—built to simplify the way you manage your money. From tracking your income and expenses to monitoring investments and downloading detailed financial reports, FinTrackr is designed to give you clarity, control, and confidence over your finances.\n\n\n" +
                        "Regards,\n\nWhether you’re planning monthly budgets, analyzing spending habits, or keeping an eye on long-term investments, FinTrackr is here to support you at every step of your financial journey.\nStart exploring your dashboard and take the first step toward smarter financial decisions—because managing money should be simple, insightful, and stress-free.\n\nWelcome aboard,\n\n" +
                        "Team FinTrackr";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);

        System.out.println("WELCOME EMAIL SENT TO: " + recipient);
    }

}
