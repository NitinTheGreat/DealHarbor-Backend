package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String name;
    private UserRole role;
    private boolean enabled;
    private boolean locked;
    private Instant createdAt;
}
