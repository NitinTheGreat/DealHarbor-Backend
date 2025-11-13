package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.MessagingService;
import com.dealharbor.dealharbor_backend.services.WebSocketMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessagingController {
    
    private final MessagingService messagingService;
    private final WebSocketMessagingService webSocketMessagingService;

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

    /**
     * Search for sellers by name (for initiating chat)
     * Supports debouncing on frontend
     */
    @GetMapping("/sellers/search")
    public ResponseEntity<PagedResponse<SellerSearchResponse>> searchSellers(
            @RequestParam String query,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(messagingService.searchSellers(query, authentication, page, size));
    }

    /**
     * Get or create conversation with a specific seller
     * This is called when clicking on seller from search or using ?sellerId query param
     */
    @GetMapping("/conversation-with-seller/{sellerId}")
    public ResponseEntity<ConversationResponse> getOrCreateConversationWithSeller(
            @PathVariable String sellerId,
            @RequestParam(required = false) String productId,
            Authentication authentication) {
        return ResponseEntity.ok(messagingService.getOrCreateConversationWithSeller(sellerId, productId, authentication));
    }

    /**
     * Get conversation details by ID
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversationById(
            @PathVariable String conversationId,
            Authentication authentication) {
        return ResponseEntity.ok(messagingService.getConversationById(conversationId, authentication));
    }

    /**
     * Delete/Archive conversation
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<?> deleteConversation(
            @PathVariable String conversationId,
            Authentication authentication) {
        messagingService.deleteConversation(conversationId, authentication);
        return ResponseEntity.ok("Conversation deleted");
    }

    // ========== WebSocket Message Handlers ==========

    /**
     * Handle incoming chat messages via WebSocket
     */
    @MessageMapping("/chat.send")
    public void sendChatMessage(@Payload ChatMessageDTO message, Principal principal) {
        String senderId = principal.getName();
        webSocketMessagingService.processAndSendMessage(message, senderId);
    }

    /**
     * Handle presence updates (online/offline/typing)
     */
    @MessageMapping("/presence")
    public void updatePresence(@Payload UserPresenceDTO presence, Principal principal) {
        // Ensure userId matches authenticated user
        presence.setUserId(principal.getName());
        webSocketMessagingService.updateUserPresence(presence);
    }

    /**
     * Handle read receipts
     */
    @MessageMapping("/message.read")
    public void markAsRead(@Payload String messageId, Principal principal) {
        String userId = principal.getName();
        webSocketMessagingService.markMessageAsRead(messageId, userId);
    }

    /**
     * Get online status of specific user (REST endpoint)
     */
    @GetMapping("/users/{userId}/online")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable String userId) {
        return ResponseEntity.ok(webSocketMessagingService.isUserOnline(userId));
    }
}
