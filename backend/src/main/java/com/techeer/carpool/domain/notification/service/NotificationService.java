package com.techeer.carpool.domain.notification.service;

import com.techeer.carpool.domain.notification.entity.Notification;
import com.techeer.carpool.domain.notification.repository.NotificationRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional
    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }

    public List<Notification> getNotifications(Long receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    public long getUnreadCount(Long receiverId) {
        return notificationRepository.countByReceiverIdAndReadAtIsNull(receiverId);
    }

    @Transactional
    public void markAsRead(Long receiverId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getReceiverId().equals(receiverId)) {
            throw new CarpoolException(ErrorCode.NOTIFICATION_FORBIDDEN);
        }
        notification.markAsRead();
    }
}
