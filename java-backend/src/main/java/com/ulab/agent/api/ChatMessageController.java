package com.ulab.agent.api;

import com.ulab.agent.managers.CallManager;
import com.ulab.agent.models.ChatMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/chat-message — the live conversation channel.
 * Python sends one POST per line: the user's words, the AI's reply,
 * and system notices. Everything lands in CallManager.handleChatMessage().
 */
@RestController
@RequestMapping("/api")
public class ChatMessageController {

    private final CallManager callManager;

    public ChatMessageController(CallManager callManager) {
        this.callManager = callManager;
    }

    @PostMapping("/chat-message")
    public ResponseEntity<String> receiveChatMessage(@RequestBody ChatMessageRequest request) {
        ChatMessage.Role role;
        try {
            role = ChatMessage.Role.valueOf(request.getRole() == null ? "SYSTEM" : request.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = ChatMessage.Role.SYSTEM;
        }
        callManager.handleChatMessage(role, request.getContent());
        return ResponseEntity.ok("Chat message received");
    }
}
