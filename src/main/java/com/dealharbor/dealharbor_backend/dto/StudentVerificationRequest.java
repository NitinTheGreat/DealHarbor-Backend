package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

@Data
public class StudentVerificationRequest {
    private String studentEmail; // Must be VIT domain
    private String universityId; // Student ID (optional)
    private Integer graduationYear;
    private String department;
}
