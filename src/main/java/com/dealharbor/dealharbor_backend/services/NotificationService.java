package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.NotificationResponse;
import com.dealharbor.dealharbor_backend.dto.PagedResponse;
import com.dealharbor.dealharbor_backend.entities.Notification;
import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.enums.NotificationType;
import com.dealharbor.dealharbor_backend.repositories.NotificationRepository;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void createNotification(String userId, String title, String message, NotificationType type, 
                                 String actionUrl, String relatedEntityId, String relatedEntityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .actionUrl(actionUrl)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();
        
        notificationRepository.save(notification);
        
        // Send email for important notifications
        if (shouldSendEmail(type)) {
            try {
                emailService.sendNotificationEmail(user.getEmail(), title, message, actionUrl);
                notification.setIsEmailSent(true);
                notificationRepository.save(notification);
            } catch (Exception e) {
                // Log error but don't fail the notification creation
                System.err.println("Failed to send notification email: " + e.getMessage());
            }
        }
    }

    public PagedResponse<NotificationResponse> getUserNotifications(Authentication authentication, int page, int size, boolean unreadOnly) {
        User user = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Notification> notificationPage;
        if (unreadOnly) {
            notificationPage = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId(), pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        }
        
        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isFirst(),
                notificationPage.isLast(),
                notificationPage.hasNext(),
                notificationPage.hasPrevious()
        );
    }

    public long getUnreadNotificationCount(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markNotificationAsRead(String notificationId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        notificationRepository.markAsRead(notificationId, user.getId());
    }

    @Transactional
    public void markAllNotificationsAsRead(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        notificationRepository.markAllAsReadForUser(user.getId());
    }

    private boolean shouldSendEmail(NotificationType type) {
        return switch (type) {
            case ORDER_CREATED, ORDER_CONFIRMED, ORDER_COMPLETED, ORDER_CANCELLED,
                 PRODUCT_APPROVED, PRODUCT_REJECTED, SECURITY_ALERT, 
                 SYSTEM_ANNOUNCEMENT -> true;
            default -> false;
        };
    }

    private NotificationResponse convertToNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getActionUrl(),
                notification.getRelatedEntityId(),
                notification.getRelatedEntityType(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
