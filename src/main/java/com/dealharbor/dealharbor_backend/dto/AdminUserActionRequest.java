package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class AdminUserActionRequest {
    private String action; // BAN, UNBAN, VERIFY_STUDENT, MARK_SPAM, DELETE_ACCOUNT
    private String reason; // Reason for action
    private Instant bannedUntil; // For temporary bans
    private Boolean isVerifiedStudent; // For student verification
}
