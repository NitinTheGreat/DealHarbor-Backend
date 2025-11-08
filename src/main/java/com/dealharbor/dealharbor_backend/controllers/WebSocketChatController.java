package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.WebSocketMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket Controller for Real-Time Messaging
 * Handles all WebSocket message types: chat, typing, read receipts, presence
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final WebSocketMessagingService messagingService;

    /**
     * Send a chat message
     * Client sends to: /app/chat.send
     * Server broadcasts to: /user/{recipientId}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO message, Principal principal) {
        try {
            log.debug("Received message from {}: {}", principal.getName(), message.getContent());
            messagingService.processAndSendMessage(message, principal.getName());
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
        }
    }

    /**
     * Typing indicator
     * Client sends to: /app/chat.typing/{conversationId}
     * Server broadcasts to: /topic/typing/{conversationId}
     */
    @MessageMapping("/chat.typing/{conversationId}")
    @SendTo("/topic/typing/{conversationId}")
    public TypingIndicatorDTO handleTyping(
            @Payload TypingIndicatorDTO typingIndicator,
            @DestinationVariable String conversationId,
            Principal principal) {
        
        typingIndicator.setUserId(principal.getName());
        log.debug("User {} typing in conversation {}: {}", 
                principal.getName(), conversationId, typingIndicator.isTyping());
        
        return typingIndicator;
    }

    /**
     * Mark message as read
     * Client sends to: /app/chat.read
     * Server broadcasts read receipt to sender
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ChatMessageDTO message, Principal principal) {
        try {
            messagingService.markMessageAsRead(message.getId(), principal.getName());
        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage(), e);
        }
    }

    /**
     * User presence update
     * Client sends to: /app/user.presence
     * Server broadcasts to: /topic/presence
     */
    @MessageMapping("/user.presence")
    @SendTo("/topic/presence")
    public UserPresenceDTO updatePresence(
            @Payload UserPresenceDTO presence,
            Principal principal) {
        
        presence.setUserId(principal.getName());
        log.debug("User {} presence: {}", principal.getName(), presence.getStatus());
        
        return messagingService.updateUserPresence(presence);
    }

    /**
     * User connects (subscribe)
     * Automatically called when user subscribes to their queue
     */
    @MessageMapping("/user.connect")
    public void userConnect(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String userId = principal.getName();
        String sessionId = headerAccessor.getSessionId();
        
        log.info("User {} connected with session {}", userId, sessionId);
        messagingService.handleUserConnect(userId, sessionId);
    }

    /**
     * User disconnects
     * Handled by WebSocket event listener
     */
}
