package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResetEligibilityResponse {
    private boolean exists;
    private boolean verified;
    private boolean eligible;
    private String message;
}
