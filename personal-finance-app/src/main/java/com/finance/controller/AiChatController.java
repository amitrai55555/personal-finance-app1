package com.finance.controller;

import com.finance.dto.chat.ChatMessageRequest;
import com.finance.dto.chat.ChatResponse;
import com.finance.security.UserPrincipal;
import com.finance.service.AiChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(Authentication authentication,
                                             @RequestBody ChatMessageRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        ChatResponse response = aiChatService.sendMessage(user.getId(), request);
        return ResponseEntity.ok(response);
    }
}
