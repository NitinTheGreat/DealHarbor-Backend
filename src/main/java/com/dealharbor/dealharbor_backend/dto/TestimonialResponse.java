package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialResponse {
    private String id;
    private String studentName;
    private String batch;
    private String quote;
    private String profilePhotoUrl;
    private Integer rating;
    private Instant createdAt;
}
