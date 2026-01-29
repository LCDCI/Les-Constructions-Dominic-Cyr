package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Notification;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer.NotificationMapper;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.NotificationResponseModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponseModel> getAllNotificationsByUserId(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationMapper::toResponseModel)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseModel> getUnreadNotificationsByUserId(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        return notifications.stream()
                .map(NotificationMapper::toResponseModel)
                .collect(Collectors.toList());
    }

    public Long getUnreadCountByUserId(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public NotificationResponseModel createNotification(UUID userId, String title, String message,
                                                        NotificationCategory category, String link) {
        Notification notification = new Notification(userId, title, message, category, link);
        notification = notificationRepository.save(notification);
        return NotificationMapper.toResponseModel(notification);
    }
}
