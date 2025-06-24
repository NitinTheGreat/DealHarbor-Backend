package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserSessionResponse {
    private String id;
    private String ipAddress;
    private String deviceInfo;
    private Instant createdAt;
    private Instant lastUsedAt;
    private boolean current;
}
