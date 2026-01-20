package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Testimonial;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, String> {
    
    List<Testimonial> findByIsFeaturedTrueAndIsApprovedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    List<Testimonial> findByIsApprovedTrueOrderByCreatedAtDesc(Pageable pageable);
}
