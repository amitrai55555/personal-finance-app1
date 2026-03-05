package com.finance.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.finance.entity.EmailVerificationToken;
import com.finance.entity.User;
import com.finance.repository.EmailVerificationTokenRepository;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final com.finance.service.Mail.EmailService emailService;
    private final Cloudinary cloudinary;

    @Autowired
    public UserProfileService(UserRepository userRepository,
                              EmailVerificationTokenRepository tokenRepository,
                              com.finance.service.Mail.EmailService emailService,
                              Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public User updateUsername(Long userId, String newUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getUsername().equalsIgnoreCase(newUsername)) {
            return user;
        }
        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username already in use");
        }

        user.setUsername(newUsername);
        return userRepository.save(user);
    }

    @Transactional
    public void requestEmailChange(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already in use");
        }

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        EmailVerificationToken emailToken = new EmailVerificationToken(user, otp, newEmail, expiresAt);
        tokenRepository.save(emailToken);

        emailService.sendEmailChangeVerification(user, newEmail, otp);
    }

    @Transactional
    public User verifyEmailOtp(String otp) {
        EmailVerificationToken emailToken = tokenRepository.findByToken(otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (emailToken.isUsed()) {
            throw new IllegalStateException("Token already used");
        }
        if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }

        User user = emailToken.getUser();
        user.setEmail(emailToken.getNewEmail());
        emailToken.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(emailToken);
        return user;
    }

    private String generateOtp() {
        return String.valueOf(100000 + new java.util.Random().nextInt(900000));
    }

    @Transactional
    public String uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "fintrackr/profile-pictures",
                        "public_id", userId + "-" + UUID.randomUUID(),
                        "overwrite", true,
                        "resource_type", "image"
                )
        );

        String url = uploadResult.get("secure_url").toString();
        user.setProfilePicture(url);
        userRepository.save(user);
        return url;
    }
}
