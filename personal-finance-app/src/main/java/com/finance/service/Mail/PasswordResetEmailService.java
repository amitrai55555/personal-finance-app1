package com.finance.service.Mail;

import com.finance.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends password reset emails with an HTML button that links to the frontend reset page.
 */
@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String frontendResetUrl;
    private final String testRecipient;
    private final String fromAddress;
    private final String fromName;

    public PasswordResetEmailService(JavaMailSender mailSender,
                                     @Value("${app.frontend.reset-password-url:http://localhost:3000/reset-password}") String frontendResetUrl,
                                     @Value("${app.mail.test-recipient:}") String testRecipient,
                                     @Value("${spring.mail.username:FinTrackrTech@gmail.com}") String fromAddress,
                                     @Value("${app.mail.from-name:FinTrackr}") String fromName) {
        this.mailSender = mailSender;
        this.frontendResetUrl = frontendResetUrl;
        this.testRecipient = testRecipient != null ? testRecipient.trim() : "";
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    public void sendPasswordResetEmail(User user, String token) {
        String recipient = (!testRecipient.isEmpty()) ? testRecipient : user.getEmail();
        String resetLink = frontendResetUrl + "?token=" + token;

        String html = """
                <p>Hi %s,</p>
                <p>We received a request to reset the password for your FinTrackr account.</p>
                <p>Click the button below to set a new password:</p>
                <p><a href="%s" style="display:inline-block;padding:12px 18px;background:#4361ee;color:#ffffff;text-decoration:none;border-radius:6px;font-weight:bold;">Reset Password</a></p>
                <p style="margin-top:16px;font-size:13px;color:#555;">
                For your security, this link is valid for a limited time. If you did not request a reset, please ignore this email.</p>
                <p>Stay secure,<br><b>Team FinTrackr</b></p>
                """.formatted(user.getFirstName(), resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(recipient);
            helper.setSubject("Password reset request");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
