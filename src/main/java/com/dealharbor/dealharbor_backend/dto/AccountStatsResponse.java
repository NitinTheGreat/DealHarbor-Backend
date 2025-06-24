package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class AccountStatsResponse {
    private int activeSessions;
    private Instant lastLogin;
    private String lastLoginIp;
    private int recentLoginAttempts;
    private boolean twoFactorEnabled;
    private Instant accountCreated;
}
