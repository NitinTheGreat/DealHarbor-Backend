package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Notification;
import com.dealharbor.dealharbor_backend.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    long countByUserIdAndIsReadFalse(String userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadForUser(String userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :notificationId AND n.user.id = :userId")
    void markAsRead(String notificationId, String userId);
}
