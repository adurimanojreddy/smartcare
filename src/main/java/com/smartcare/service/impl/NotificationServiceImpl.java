package com.smartcare.service.impl;

import com.smartcare.dto.notification.NotificationResponse;
import com.smartcare.entity.Notification;
import com.smartcare.entity.User;
import com.smartcare.entity.enums.NotificationType;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.mapper.NotificationMapper;
import com.smartcare.repository.NotificationRepository;
import com.smartcare.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse create(User recipient, NotificationType type, String title, String message,
                                        String referenceId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .read(false)
                .build();
        return NotificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadForUser(Long userId) {
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new UnauthorizedAccessException("This notification does not belong to you");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
