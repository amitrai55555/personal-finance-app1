package com.finance.service.Mail;

import com.finance.entity.EmailUpdateOtpToken;
import com.finance.entity.User;
import com.finance.repository.EmailUpdateOtpTokenRepository;
import com.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class EmailUpdateOtpService {

    private final EmailUpdateOtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public EmailUpdateOtpService(EmailUpdateOtpTokenRepository otpTokenRepository,
            EmailService emailService,
            UserRepository userRepository) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /**
     * Generate a 6-digit OTP and send it to the new email address.
     */
    public void generateAndSendOtp(User user, String newEmail) {
        // Check if the new email is already in use by another user
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("This email is already in use by another account");
        }

        String otp = generateOtp();

        EmailUpdateOtpToken token = new EmailUpdateOtpToken();
        token.setUser(user);
        token.setNewEmail(newEmail);
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);

        otpTokenRepository.save(token);

        // Send OTP to the NEW email address
        emailService.sendEmailChangeVerification(user, newEmail, otp);

        System.out.println("EMAIL UPDATE OTP SENT TO: " + newEmail);
    }

    /**
     * Verify OTP and update the user's email if valid.
     */
    public User verifyOtpAndUpdateEmail(User user, String newEmail, String otp) {
        EmailUpdateOtpToken token = otpTokenRepository
                .findTopByUserAndNewEmailAndUsedFalseOrderByCreatedAtDesc(user, newEmail)
                .orElseThrow(() -> new RuntimeException("OTP not found. Please request a new one."));

        if (token.isExpired()) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!token.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        // Double-check email uniqueness at the time of update
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("This email is already in use by another account");
        }

        // Mark OTP as used
        token.setUsed(true);
        otpTokenRepository.save(token);

        // Update user's email
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
