package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private String otherUserId;
    private String otherUserName;
    private String otherUserProfilePhoto;
    private String productId;
    private String productTitle;
    private String productImageUrl;
    private String orderId;
    private String lastMessage;
    private Instant lastMessageAt;
    private long unreadCount;
    private boolean isActive;
}
