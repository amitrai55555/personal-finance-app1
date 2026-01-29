package com.finance.service.Mail;

import com.finance.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String frontendResetUrl;
    private final String testRecipient;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.frontend.reset-password-url:http://localhost:3000/reset-password}") String frontendResetUrl,
            @Value("${app.mail.test-recipient:}") String testRecipient
    ) {
        this.mailSender = mailSender;
        this.frontendResetUrl = frontendResetUrl;
        this.testRecipient = testRecipient != null ? testRecipient.trim() : "";

        System.out.println(
                "EmailService initialized | resetUrl=" + this.frontendResetUrl +
                        " | testRecipient=" + this.testRecipient
        );
    }


    public void sendPasswordResetEmail(User user, String token) {

        String recipient =
                (!testRecipient.isEmpty()) ? testRecipient : user.getEmail();

        String resetLink = frontendResetUrl + "?token=" + token;
        String subject = "Password reset request";

        String html =
                "<p>Hi " + user.getFirstName() + ",</p>" +
                        "<p>Greetings from <b>FinTrackr</b>!</p>" +
                        "<p>We received a request to reset the password for your FinTrackr account.</p>" +
                        "<p>Click the button below to set a new password:</p>" +

                        "<a href='" + resetLink + "' " +
                        "style='display:inline-block;padding:12px 18px;" +
                        "background:#4361ee;color:#ffffff;" +
                        "text-decoration:none;border-radius:6px;" +
                        "font-weight:bold;'>Reset Password</a>" +

                        "<p style='margin-top:16px;font-size:13px;color:#555;'>" +
                        "For your security, this link is valid for a limited time. " +
                        "If you did not request a password reset, please ignore this email—" +
                        "your account will remain secure.</p>" +

                        "<p>If you need any assistance, our support team is always here to help.</p>" +

                        "<p>Stay secure and in control of your finances,<br>" +
                        "<b>Team FinTrackr</b></p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom("FinTrackrTech@gmail.com","FinTrackr");
            helper.setTo(recipient);
            helper.setSubject(subject);

            // 🔥 THIS LINE MAKES HTML WORK
            helper.setText(html, true);
            mailSender.send(message);

            System.out.println("PASSWORD RESET EMAIL SENT TO: " + recipient);

        }  catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }


    public void sendWelcomeEmail(User user) {

        String recipient =
                (!testRecipient.isEmpty()) ? testRecipient : user.getEmail();

        String subject = "Welcome to FinTrackr 🎉";

        String text =
                "Hi " + user.getFirstName() + ",\n\n" +
                        "Thank you for choosing FinTrackr. We’re excited to welcome you to our community.\n\n" +
                        "FinTrackr helps you track income, expenses, investments, and download detailed reports—" +
                        "all designed to give you clarity and control over your finances.\n\n" +
                        "Start exploring your dashboard and take the first step toward smarter financial decisions.\n\n" +
                        "Welcome aboard,\n\n" +
                        "Team FinTrackr";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("FinTrackr <FinTrackrTech@gmail.com>");
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);

        System.out.println("WELCOME EMAIL SENT TO: " + recipient);
    }


    public void sendOtpVerificationEmail(User user, String otp) {
        System.out.println("hit1");
        String recipient =
                (testRecipient != null && !testRecipient.isEmpty())
                        ? testRecipient
                        : user.getEmail();

        String subject = "Bank Account Verification OTP";

        String text =
                "Hi " + user.getFirstName() + ",\n\n" +
                        "Your One-Time Password (OTP) for verifying your FinTrackr account is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for the next 10 minutes. Please do not share it with anyone for security reasons..\n\n" +
                        "If you did not request this, please ignore this email.\n\n"+
                "Stay secure,\n" +
                        "Team FinTrackr";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);

        System.out.println("OTP EMAIL SENT TO: " + recipient);
    }




}
