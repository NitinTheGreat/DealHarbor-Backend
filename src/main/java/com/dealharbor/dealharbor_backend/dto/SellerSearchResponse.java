package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for seller search results in messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerSearchResponse {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private Boolean isVerified;
    private Integer productCount;
    private Double averageRating;
    private Integer totalReviews;
    private Instant lastActive;
    private Boolean isOnline;
    
    // Conversation status
    private Boolean hasExistingConversation;
    private String existingConversationId;
    private Instant lastMessageTime;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
