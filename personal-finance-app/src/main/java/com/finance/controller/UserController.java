package com.finance.controller;

import com.finance.service.UserRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")

public class UserController {


        @Autowired
        private UserRegisterService userService;

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

}
