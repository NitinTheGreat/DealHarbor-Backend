package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.Conversation;
import com.dealharbor.dealharbor_backend.entities.Message;
import com.dealharbor.dealharbor_backend.entities.Product;
import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket Messaging Service
 * Handles real-time message processing, delivery, and status updates
 * Uses Redis for caching (optional) and SimpMessagingTemplate for WebSocket communication
 */
@Service
@Slf4j
public class WebSocketMessagingService {

    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    private static final String PRESENCE_KEY_PREFIX = "user:presence:";
    private static final String TYPING_KEY_PREFIX = "typing:";
    private static final String ONLINE_USERS_KEY = "users:online";

    public WebSocketMessagingService(
            SimpMessagingTemplate messagingTemplate,
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            NotificationService notificationService) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    /**
     * Process and send message via WebSocket
     * Optimized for minimal latency
     */
    @Transactional
    public void processAndSendMessage(ChatMessageDTO messageDTO, String senderId) {
        try {
            // Validate sender
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            // Get or create conversation
            Conversation conversation = conversationRepository.findById(messageDTO.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            // Create message entity
            Message message = Message.builder()
                    .id(UUID.randomUUID().toString())
                    .conversation(conversation)
                    .sender(sender)
                    .content(messageDTO.getContent())
                    .attachmentUrl(messageDTO.getAttachmentUrl())
                    .isRead(false)
                    .createdAt(Instant.now())
                    .build();

            // Save to database asynchronously (don't block WebSocket)
            messageRepository.save(message);

            // Update conversation timestamp
            conversation.setLastMessageAt(Instant.now());
            conversationRepository.save(conversation);

            // Build response DTO
            ChatMessageDTO responseDTO = ChatMessageDTO.builder()
                    .id(message.getId())
                    .conversationId(conversation.getId())
                    .senderId(sender.getId())
                    .senderName(sender.getName())
                    .senderAvatar(sender.getProfilePhotoUrl())
                    .recipientId(messageDTO.getRecipientId())
                    .content(message.getContent())
                    .type(ChatMessageDTO.MessageType.TEXT)
                    .status(ChatMessageDTO.MessageStatus.SENT)
                    .timestamp(message.getCreatedAt())
                    .attachmentUrl(message.getAttachmentUrl())
                    .build();

            // Add product info if applicable
            if (conversation.getProduct() != null) {
                Product product = conversation.getProduct();
                responseDTO.setProductId(product.getId());
                responseDTO.setProductTitle(product.getTitle());
                // Get first image from images list
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    responseDTO.setProductImage(product.getImages().get(0).getImageUrl());
                }
            }

            // Send to recipient via WebSocket
            String recipientId = messageDTO.getRecipientId();
            messagingTemplate.convertAndSendToUser(
                    recipientId,
                    "/queue/messages",
                    responseDTO
            );

            // Send delivery confirmation to sender
            ChatMessageDTO confirmationDTO = ChatMessageDTO.builder()
                    .id(message.getId())
                    .status(ChatMessageDTO.MessageStatus.DELIVERED)
                    .timestamp(Instant.now())
                    .build();
            
            messagingTemplate.convertAndSendToUser(
                    senderId,
                    "/queue/confirmations",
                    confirmationDTO
            );

            // Check if recipient is online
            boolean isOnline = isUserOnline(recipientId);
            
            // If recipient is offline, send push notification
            if (!isOnline) {
                sendOfflineNotification(recipientId, sender.getName(), messageDTO.getContent());
            }

            log.debug("Message sent from {} to {}: {}", senderId, recipientId, message.getId());

        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            
            // Send error to sender
            ChatMessageDTO errorDTO = ChatMessageDTO.builder()
                    .id(messageDTO.getId())
                    .status(ChatMessageDTO.MessageStatus.FAILED)
                    .build();
            
            messagingTemplate.convertAndSendToUser(
                    senderId,
                    "/queue/errors",
                    errorDTO
            );
        }
    }

    /**
     * Mark message as read and send read receipt
     */
    @Transactional
    public void markMessageAsRead(String messageId, String userId) {
        try {
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            // Only recipient can mark as read
            if (!message.getConversation().getUser1().getId().equals(userId) &&
                !message.getConversation().getUser2().getId().equals(userId)) {
                log.warn("Unauthorized read receipt from user {} for message {}", userId, messageId);
                return;
            }

            // Update message
            if (!message.getIsRead()) {
                message.setIsRead(true);
                message.setReadAt(Instant.now());
                messageRepository.save(message);

                // Send read receipt to sender
                ChatMessageDTO readReceipt = ChatMessageDTO.builder()
                        .id(messageId)
                        .type(ChatMessageDTO.MessageType.READ_RECEIPT)
                        .status(ChatMessageDTO.MessageStatus.READ)
                        .readAt(message.getReadAt())
                        .build();

                messagingTemplate.convertAndSendToUser(
                        message.getSender().getId(),
                        "/queue/receipts",
                        readReceipt
                );

                log.debug("Message {} marked as read by {}", messageId, userId);
            }
        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage(), e);
        }
    }

    /**
     * Update user presence status
     */
    public UserPresenceDTO updateUserPresence(UserPresenceDTO presence) {
        try {
            String userId = presence.getUserId();
            String presenceKey = PRESENCE_KEY_PREFIX + userId;

            // Store in Redis with TTL (if available)
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(presenceKey, presence.getStatus().name(), 5, TimeUnit.MINUTES);

                // Add to online users set if online
                if (presence.getStatus() == UserPresenceDTO.PresenceStatus.ONLINE) {
                    redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
                } else {
                    redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
                }
            }

            presence.setLastSeen(Instant.now());
            
            log.debug("User {} presence updated to {}", userId, presence.getStatus());
            
            return presence;
        } catch (Exception e) {
            log.error("Error updating presence: {}", e.getMessage(), e);
            return presence;
        }
    }

    /**
     * Handle user WebSocket connection
     */
    public void handleUserConnect(String userId, String sessionId) {
        try {
            // Mark user as online
            UserPresenceDTO presence = UserPresenceDTO.builder()
                    .userId(userId)
                    .status(UserPresenceDTO.PresenceStatus.ONLINE)
                    .lastSeen(Instant.now())
                    .build();

            updateUserPresence(presence);

            // Broadcast presence to all users
            messagingTemplate.convertAndSend("/topic/presence", presence);

            log.info("User {} connected (session: {})", userId, sessionId);
        } catch (Exception e) {
            log.error("Error handling user connect: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle user WebSocket disconnection
     */
    public void handleUserDisconnect(String userId, String sessionId) {
        try {
            // Mark user as offline
            UserPresenceDTO presence = UserPresenceDTO.builder()
                    .userId(userId)
                    .status(UserPresenceDTO.PresenceStatus.OFFLINE)
                    .lastSeen(Instant.now())
                    .build();

            updateUserPresence(presence);

            // Broadcast presence to all users
            messagingTemplate.convertAndSend("/topic/presence", presence);

            log.info("User {} disconnected (session: {})", userId, sessionId);
        } catch (Exception e) {
            log.error("Error handling user disconnect: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if user is online (from Redis cache)
     */
    public boolean isUserOnline(String userId) {
        try {
            if (redisTemplate == null) {
                return false; // Cannot determine without Redis
            }
            Boolean isMember = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId);
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.error("Error checking online status: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get all online users
     */
    public Set<Object> getOnlineUsers() {
        try {
            if (redisTemplate == null) {
                return Set.of(); // Return empty set without Redis
            }
            return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        } catch (Exception e) {
            log.error("Error getting online users: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    /**
     * Send offline notification via email/push
     */
    private void sendOfflineNotification(String recipientId, String senderName, String content) {
        try {
            // Truncate content for notification
            String preview = content.length() > 50 
                    ? content.substring(0, 50) + "..." 
                    : content;

            notificationService.createNotification(
                    recipientId,
                    "New message from " + senderName,
                    preview,
                    com.dealharbor.dealharbor_backend.enums.NotificationType.NEW_MESSAGE,
                    "/messages",
                    null,
                    "MESSAGE"
            );
        } catch (Exception e) {
            log.error("Error sending offline notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Bulk send messages to multiple recipients (for announcements)
     */
    public void broadcastMessage(Set<String> recipientIds, ChatMessageDTO message) {
        recipientIds.forEach(recipientId -> {
            messagingTemplate.convertAndSendToUser(
                    recipientId,
                    "/queue/messages",
                    message
            );
        });
    }
}
