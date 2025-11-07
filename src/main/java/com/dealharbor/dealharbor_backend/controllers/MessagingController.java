package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.MessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessagingController {
    
    private final MessagingService messagingService;

    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> startConversation(
            @RequestParam String otherUserId,
            @RequestParam(required = false) String productId,
            Authentication authentication) {
        return ResponseEntity.ok(messagingService.startConversation(otherUserId, productId, authentication));
    }

    @GetMapping("/conversations")
    public ResponseEntity<PagedResponse<ConversationResponse>> getUserConversations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(messagingService.getUserConversations(authentication, page, size));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable String conversationId,
            @RequestBody MessageRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(messagingService.sendMessage(conversationId, request, authentication));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<PagedResponse<MessageResponse>> getConversationMessages(
            @PathVariable String conversationId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messagingService.getConversationMessages(conversationId, authentication, page, size));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<?> markConversationAsRead(
            @PathVariable String conversationId,
            Authentication authentication) {
        messagingService.markConversationAsRead(conversationId, authentication);
        return ResponseEntity.ok("Conversation marked as read");
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(Authentication authentication) {
        return ResponseEntity.ok(messagingService.getUnreadMessageCount(authentication));
    }
}
