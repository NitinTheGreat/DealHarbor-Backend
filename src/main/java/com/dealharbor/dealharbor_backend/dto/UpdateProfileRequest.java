package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String bio;
    private String phoneNumber;
}
