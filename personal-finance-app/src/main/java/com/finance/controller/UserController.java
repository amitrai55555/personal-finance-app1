package com.finance.controller;

import com.finance.security.UserPrincipal;
import com.finance.service.UserProfileService;
import com.finance.service.UserRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")

public class UserController {


        @Autowired
        private UserRegisterService userService;

        @Autowired
        private UserProfileService userProfileService;

        @GetMapping("/check-username")
        public ResponseEntity<?> checkUsername(@RequestParam String username) {

            boolean exists = userService.checkUsernameExists(username);

            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);

            if (exists) {
                response.put("message", "Username already exists");
            } else {
                response.put("message", "Username available");
            }

            return ResponseEntity.ok(response);
        }

        @PutMapping("/username")
        public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> body, Authentication authentication) {
            String newUsername = body.get("username");
            if (newUsername == null || newUsername.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "username is required"));
            }
            try {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                userProfileService.updateUsername(principal.getId(), newUsername.trim());
                return ResponseEntity.ok(Map.of("message", "Username updated"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
            }
        }

        @PutMapping("/email")
        public ResponseEntity<?> requestEmailChange(@RequestBody Map<String, String> body,
                                                    Authentication authentication) {
            String newEmail = body.get("email");
            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
            }
            try {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                userProfileService.requestEmailChange(principal.getId(), newEmail.trim());
                return ResponseEntity.ok(Map.of("message", "Verification code sent"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
            }
        }

        @PostMapping("/email/verify")
        public ResponseEntity<?> verifyEmail(@RequestParam("otp") String otp) {
            if (otp == null || otp.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "otp is required"));
            }
            try {
                userProfileService.verifyEmailOtp(otp);
                return ResponseEntity.ok(Map.of("message", "Email verified and updated"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } catch (IllegalStateException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
            }
        }

        @PostMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> uploadProfilePicture(@RequestPart("file") MultipartFile file,
                                                      Authentication authentication) {
            try {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                String path = userProfileService.uploadProfilePicture(principal.getId(), file);
                return ResponseEntity.ok(Map.of("message", "Profile picture updated", "path ", path));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to upload profile picture", "details", e.getMessage()));
            }
        }

}
