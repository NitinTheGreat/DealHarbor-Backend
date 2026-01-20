package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.TestimonialResponse;
import com.dealharbor.dealharbor_backend.entities.Testimonial;
import com.dealharbor.dealharbor_backend.repositories.TestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestimonialService {
    
    private final TestimonialRepository testimonialRepository;

    /**
     * Get testimonials for homepage social proof section
     */
    public List<TestimonialResponse> getTestimonials(int limit, boolean featuredOnly) {
        Pageable pageable = PageRequest.of(0, limit);
        
        List<Testimonial> testimonials;
        if (featuredOnly) {
            testimonials = testimonialRepository.findByIsFeaturedTrueAndIsApprovedTrueOrderByCreatedAtDesc(pageable);
        } else {
            testimonials = testimonialRepository.findByIsApprovedTrueOrderByCreatedAtDesc(pageable);
        }
        
        return testimonials.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private TestimonialResponse convertToResponse(Testimonial testimonial) {
        return TestimonialResponse.builder()
                .id(testimonial.getId())
                .studentName(testimonial.getStudentName())
                .batch(testimonial.getBatch())
                .quote(testimonial.getQuote())
                .profilePhotoUrl(testimonial.getProfilePhotoUrl())
                .rating(testimonial.getRating())
                .createdAt(testimonial.getCreatedAt())
                .build();
    }
}
