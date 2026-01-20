package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.TestimonialResponse;
import com.dealharbor.dealharbor_backend.services.TestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class TestimonialController {
    
    private final TestimonialService testimonialService;

    /**
     * Get testimonials for homepage social proof section
     * Public endpoint - no authentication required
     */
    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> getTestimonials(
            @RequestParam(defaultValue = "3") int limit,
            @RequestParam(defaultValue = "true") boolean featured) {
        return ResponseEntity.ok(testimonialService.getTestimonials(limit, featured));
    }
}
