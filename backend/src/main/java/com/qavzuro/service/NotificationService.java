package com.qavzuro.service;

import com.qavzuro.domain.Notification;
import com.qavzuro.domain.NotificationType;
import com.qavzuro.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notify(String userId, NotificationType type, String title, String message, String referenceId) {
        Notification n = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(n);
    }

    public Page<Notification> list(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long unreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markRead(String userId, String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    public void markAllRead(String userId) {
        var page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged());
        page.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(page);
    }
}
