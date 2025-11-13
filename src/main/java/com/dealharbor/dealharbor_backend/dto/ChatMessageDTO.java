package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * WebSocket message payload for real-time chat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String recipientId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private String productId;
    private String productTitle;
    private String productImage;
    private Instant timestamp;
    private Instant readAt;
    private String replyToId;
    
    // For file attachments
    private String attachmentUrl;
    private String attachmentType;
    private Long attachmentSize;
    
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        PRODUCT,
        SYSTEM,
        TYPING,
        READ_RECEIPT
    }
    
    public enum MessageStatus {
        SENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }
}
