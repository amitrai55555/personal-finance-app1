package com.finance.controller;

import com.finance.entity.User;
import com.finance.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        resp.put("message", "Admin access verified");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        Map<String, Object> resp = new HashMap<>();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();

        resp.put("totalUsers", totalUsers);
        resp.put("activeUsers", activeUsers);

        // Placeholder metrics for the current UI; can be replaced when you add real admin reporting.
        resp.put("totalReports", 0);
        resp.put("systemHealth", 100);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AdminUserDto> dtos = users.stream().map(AdminUserDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {

        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }
        if (req.email == null || req.email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
        }
        if (req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "password is required"));
        }

        String username = (req.username == null || req.username.isBlank())
                ? deriveUsername(req.email)
                : req.username.trim();

        NameParts nameParts = deriveName(req);

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username is already taken"));
        }
        if (userRepository.existsByEmail(req.email.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email is already in use"));
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(req.email.trim());
        user.setPassword(passwordEncoder.encode(req.password));
        user.setFirstName(nameParts.firstName);
        user.setLastName(nameParts.lastName);
        user.setRole(parseRole(req.role));
        user.setEnabled(req.enabled == null ? Boolean.TRUE : req.enabled);

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(AdminUserDto.from(saved));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req, Authentication auth) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
        }


        if (auth != null && auth.getName() != null && auth.getName().equalsIgnoreCase(user.getUsername())) {
            if (req.enabled != null && !req.enabled) {
                return ResponseEntity.badRequest().body(Map.of("error", "You cannot disable your own account"));
            }
        }

        if (req.firstName != null) user.setFirstName(req.firstName.trim());
        if (req.lastName != null) user.setLastName(req.lastName.trim());
        if (req.enabled != null) user.setEnabled(req.enabled);
        if (req.role != null && !req.role.isBlank()) user.setRole(parseRole(req.role));

        User saved = userRepository.save(user);
        return ResponseEntity.ok(AdminUserDto.from(saved));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication auth) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

     
        if (auth != null && auth.getName() != null && auth.getName().equalsIgnoreCase(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot delete your own account"));
        }

        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    private static String deriveUsername(String email) {
        String base = email.trim();
        int at = base.indexOf('@');
        if (at > 0) base = base.substring(0, at);
        base = base.replaceAll("[^a-zA-Z0-9._-]", "");
        return base.isBlank() ? ("user" + System.currentTimeMillis()) : base;
    }

    private static NameParts deriveName(CreateUserRequest req) {
        String first = safe(req.firstName);
        String last = safe(req.lastName);

        if ((!first.isBlank() || !last.isBlank())) {
            return new NameParts(first.isBlank() ? "User" : first, last.isBlank() ? "" : last);
        }

        String name = safe(req.name);
        if (name.isBlank()) {
            return new NameParts("User", "");
        }

        String[] parts = name.trim().split("\\s+", 2);
        String f = parts[0];
        String l = parts.length > 1 ? parts[1] : "";
        return new NameParts(f, l);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static User.Role parseRole(String role) {
        if (role == null) return User.Role.USER;
        String r = role.trim().toUpperCase(Locale.ROOT);
        if (r.startsWith("ROLE_")) r = r.substring("ROLE_".length());
        return "ADMIN".equals(r) ? User.Role.ADMIN : User.Role.USER;
    }

    private record NameParts(String firstName, String lastName) {}

    public static class CreateUserRequest {
        public String name;

        @NotBlank
        public String username;

        @Email
        public String email;

        public String firstName;
        public String lastName;

        @NotBlank
        public String password;

        public String role; // ADMIN | USER (also supports ROLE_ADMIN)
        public Boolean enabled;
    }

    public static class UpdateUserRequest {
        public String firstName;
        public String lastName;
        public String role;
        public Boolean enabled;
    }

    public record AdminUserDto(Long id, String username, String email, String firstName, String lastName, String role, Boolean enabled, Object createdAt) {
        public static AdminUserDto from(User u) {
            return new AdminUserDto(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.getRole() == null ? null : u.getRole().name(),
                    u.getEnabled(),
                    u.getCreatedAt()
            );
        }
    }
}
