package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.AdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminActionRepository extends JpaRepository<AdminAction, String> {
    Page<AdminAction> findByOrderByCreatedAtDesc(Pageable pageable);
    List<AdminAction> findByAdminIdOrderByCreatedAtDesc(String adminId);
    List<AdminAction> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId);
    Page<AdminAction> findByActionTypeOrderByCreatedAtDesc(String actionType, Pageable pageable);
}
