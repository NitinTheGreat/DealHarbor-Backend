package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckEmailResponse {
    private boolean exists;
    private boolean verified;
}
