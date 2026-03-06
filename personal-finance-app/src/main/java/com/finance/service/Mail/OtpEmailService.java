package com.finance.service.Mail;

import com.finance.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends OTP emails for bank account verification.
 */
@Service
public class OtpEmailService {

    private final JavaMailSender mailSender;
    private final String testRecipient;
    private final String fromAddress;

    public OtpEmailService(JavaMailSender mailSender,
                           @Value("${app.mail.test-recipient:}") String testRecipient,
                           @Value("${spring.mail.username:FinTrackrTech@gmail.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.testRecipient = testRecipient != null ? testRecipient.trim() : "";
        this.fromAddress = fromAddress;
    }

    public void sendOtpVerificationEmail(User user, String otp) {
        String recipient = (!testRecipient.isEmpty()) ? testRecipient : user.getEmail();

        String subject = "Bank Account Verification OTP";
        String text = """
                Hi %s,

                Your One-Time Password (OTP) for verifying your FinTrackr account is:

                %s

                This OTP is valid for the next 10 minutes. Please do not share it with anyone.

                If you did not request this, please ignore this email.

                Stay secure,
                Team FinTrackr
                """.formatted(user.getUsername(), otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
