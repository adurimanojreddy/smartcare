package com.smartcare.service;

import com.smartcare.dto.notification.NotificationResponse;
import com.smartcare.entity.User;
import com.smartcare.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationResponse create(User recipient, NotificationType type, String title, String message, String referenceId);

    List<NotificationResponse> getForUser(Long userId);

    List<NotificationResponse> getUnreadForUser(Long userId);

    void markAsRead(Long userId, Long notificationId);
}
