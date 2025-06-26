package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private String actionUrl;
    private String relatedEntityId;
    private String relatedEntityType;
    private boolean isRead;
    private Instant createdAt;
    private Instant readAt;
}
