package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SecurityEventResponse {
    private String eventType;
    private String ipAddress;
    private String description;
    private Instant timestamp;
}
