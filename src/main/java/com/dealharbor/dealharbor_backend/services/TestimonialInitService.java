package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.entities.Testimonial;
import com.dealharbor.dealharbor_backend.repositories.TestimonialRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestimonialInitService {
    
    private final TestimonialRepository testimonialRepository;

    @PostConstruct
    public void initializeTestimonials() {
        if (testimonialRepository.count() > 0) {
            log.info("Testimonials already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample testimonials...");

        testimonialRepository.save(Testimonial.builder()
                .studentName("Deepanshu")
                .batch("CSE 2025")
                .quote("Sold my old textbooks within a day. Way better than those random Facebook groups.")
                .rating(5)
                .isFeatured(true)
                .isApproved(true)
                .createdAt(Instant.now())
                .build());

        testimonialRepository.save(Testimonial.builder()
                .studentName("Naif")
                .batch("ECE 2024")
                .quote("Got a barely used laptop for half the price. The seller was from my hostel so pickup was easy.")
                .rating(5)
                .isFeatured(true)
                .isApproved(true)
                .createdAt(Instant.now())
                .build());

        testimonialRepository.save(Testimonial.builder()
                .studentName("Abhay Zalaki")
                .batch("IT 2025")
                .quote("Finally a platform where I can trust the sellers. Everyone here is a verified student.")
                .rating(5)
                .isFeatured(true)
                .isApproved(true)
                .createdAt(Instant.now())
                .build());

        testimonialRepository.save(Testimonial.builder()
                .studentName("Mahin")
                .batch("MECH 2024")
                .quote("Bought a cycle and sold my old phone. Both deals went smooth, no hassle at all.")
                .rating(5)
                .isFeatured(true)
                .isApproved(true)
                .createdAt(Instant.now())
                .build());

        log.info("Sample testimonials initialized successfully");
    }
}
