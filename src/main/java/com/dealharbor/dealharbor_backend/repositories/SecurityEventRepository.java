package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, String> {
    List<SecurityEvent> findByUserIdOrderByTimestampDesc(String userId);
    List<SecurityEvent> findTop10ByUserIdOrderByTimestampDesc(String userId);
}
