package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Typing indicator payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDTO {
    private String conversationId;
    private String userId;
    private String userName;
    private boolean isTyping;
    private Instant timestamp;
}
