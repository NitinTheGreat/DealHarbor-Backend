package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

@Data
public class DeleteAccountRequest {
    private String password;
    private String reason; // Optional reason for deletion
}
