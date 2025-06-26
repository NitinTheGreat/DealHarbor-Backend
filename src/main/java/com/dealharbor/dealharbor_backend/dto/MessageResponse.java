package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private String senderId;
    private String senderName;
    private String senderProfilePhoto;
    private String content;
    private MessageType messageType;
    private String attachmentUrl;
    private boolean isRead;
    private boolean isEdited;
    private Instant createdAt;
    private Instant readAt;
}
