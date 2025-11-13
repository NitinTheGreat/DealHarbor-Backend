package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User presence/online status payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresenceDTO {
    private String userId;
    private PresenceStatus status;
    private Instant lastSeen;
    
    public enum PresenceStatus {
        ONLINE,
        AWAY,
        OFFLINE
    }
}
