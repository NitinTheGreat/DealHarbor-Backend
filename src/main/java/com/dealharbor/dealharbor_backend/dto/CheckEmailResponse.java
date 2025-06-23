package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckEmailResponse {
    private boolean exists;
    private boolean verified;
}
