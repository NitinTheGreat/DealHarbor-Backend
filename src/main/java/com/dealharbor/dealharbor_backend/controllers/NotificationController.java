package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.NotificationResponse;
import com.dealharbor.dealharbor_backend.dto.PagedResponse;
import com.dealharbor.dealharbor_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PagedResponse<NotificationResponse>> getUserNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.getUserNotifications(authentication, page, size, unreadOnly));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationCount(authentication));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(
            @PathVariable String notificationId,
            Authentication authentication) {
        notificationService.markNotificationAsRead(notificationId, authentication);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead(Authentication authentication) {
        notificationService.markAllNotificationsAsRead(authentication);
        return ResponseEntity.ok("All notifications marked as read");
    }
}
