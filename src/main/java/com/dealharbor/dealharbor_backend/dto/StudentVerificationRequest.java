package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

@Data
public class StudentVerificationRequest {
    private String studentEmail; // Must be VIT domain
    private String universityId; // Optional - Student ID
    private Integer graduationYear; // Optional
    private String department; // Optional
}
