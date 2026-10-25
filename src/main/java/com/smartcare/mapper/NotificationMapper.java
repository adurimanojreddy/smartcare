package com.smartcare.mapper;

import com.smartcare.dto.notification.NotificationResponse;
import com.smartcare.entity.Notification;

public final class NotificationMapper {
    private NotificationMapper() {}

    public static NotificationResponse toResponse(Notification n) {
        if (n == null) return null;
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
