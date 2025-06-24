package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

@Data
public class StudentEmailOtpVerifyRequest {
    private String studentEmail;
    private String otp;
    private String universityId; // Optional
    private Integer graduationYear; // Optional
    private String department; // Optional
}
