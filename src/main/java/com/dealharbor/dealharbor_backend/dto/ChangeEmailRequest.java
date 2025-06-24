package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

@Data
public class ChangeEmailRequest {
    private String newEmail;
    private String password;
}
