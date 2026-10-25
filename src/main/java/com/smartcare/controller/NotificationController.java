package com.smartcare.controller;

import com.smartcare.dto.notification.NotificationResponse;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** In-app notifications for the logged-in user (any role) - Module 13/21. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getMine() {
        return ResponseEntity.ok(notificationService.getForUser(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getUnread() {
        return ResponseEntity.ok(notificationService.getUnreadForUser(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
