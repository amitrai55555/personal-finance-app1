package com.finance.controller;

import com.finance.dto.auth.JwtResponse;
import com.finance.dto.auth.LoginRequest;
import com.finance.dto.auth.SignupRequest;
import com.finance.entity.User;
import com.finance.repository.UserRepository;
import com.finance.security.JwtUtils;
import com.finance.security.UserPrincipal;
import com.finance.service.Mail.EmailService;
import com.finance.service.Mail.PasswordResetService;
import jakarta.validation.Valid;
import org.aspectj.weaver.patterns.IToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.cloudinary.AccessControlRule.AccessType.token;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
class AuthControllerReset {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetService passwordResetService;
    private final Logger logger = LoggerFactory.getLogger(AuthControllerReset.class);
    @Autowired
    private EmailService emailService;
    public AuthControllerReset(AuthenticationManager authenticationManager,
                               UserRepository userRepository,
                               PasswordEncoder encoder,
                               JwtUtils jwtUtils,
                               PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("🔍 Login attempt for: " + loginRequest.getUsernameOrEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    getUserFirstName(userDetails.getId()),
                    getUserLastName(userDetails.getId()),
                    userDetails.getAuthorities().iterator().next().getAuthority()));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username/email or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        Map<String, String> response = new HashMap<>();

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            response.put("error", "Username is already taken!");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            response.put("error", "Email is already in use!");
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName()
        );

        User savedUser = userRepository.save(user);

        emailService.sendWelcomeEmail(savedUser);

        response.put("message", "User registered successfully!");

        return ResponseEntity.ok(response);
    }




    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("role", user.getRole());
        userInfo.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(userInfo);
    }

    private String getUserFirstName(Long userId) {
        return userRepository.findById(userId).map(User::getFirstName).orElse("");
    }

    private String getUserLastName(Long userId) {
        return userRepository.findById(userId).map(User::getLastName).orElse("");
    }

    // DTOs (with getters/setters) for proper binding and validation
    public static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    /**
     * Forgot password handler
     * - Validates request
     * - Calls service to create token and send email
     * - Returns generic 200 for security (so attackers can't enumerate emails)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
        }

        try {
            // Call service to create token and send the email.
            // Note: we intentionally do NOT reveal in the response whether the email exists.
            // If your service returns the token (useful for dev), it will be ignored here.
            passwordResetService.createPasswordResetTokenForEmail(request.getEmail());

            // DEV: log token creation attempt (service should log/return token if you want to test easily)
            logger.info("Password reset requested for email: {}", request.getEmail());

            // Generic response for security
            return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
        } catch (Exception ex) {
            // Log but return a generic response — don't reveal internal details
            logger.error("Error while creating password reset token for email: " + request.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to process request"));
        }
    }


    /**
     * Reset password handler
     * - Validates request
     * - Calls service to reset password
     * - Returns appropriate responses based on success/failure
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "token is required"));
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "newPassword is required"));
        }

        try {
            // Call service. If service returns boolean or throws exceptions, handle accordingly.
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

            // If the service returns normally, assume success
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException rex) {
            // If your service throws RuntimeException with message "Invalid token" map to 404.
            String msg = rex.getMessage() == null ? "" : rex.getMessage().toLowerCase();
            if (msg.contains("invalid token") || msg.contains("token invalid") || msg.contains("expired")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Invalid or expired token"));
            } else {
                logger.error("Runtime error while resetting password for token: {}", request.getToken(), rex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Unable to reset password"));
            }
        } catch (Exception ex) {
            logger.error("Unexpected error while resetting password for token: {}", request.getToken(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to reset password"));
        }
    }
}
