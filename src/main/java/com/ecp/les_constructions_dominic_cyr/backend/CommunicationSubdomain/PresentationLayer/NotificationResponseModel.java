package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationResponseModel {
    private UUID notificationId;
    private String title;
    private String message;
    private NotificationCategory category;
    private String link;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponseModel() {
    }

    public NotificationResponseModel(UUID notificationId, String title, String message,
                                     NotificationCategory category, String link,
                                     Boolean isRead, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.title = title;
        this.message = message;
        this.category = category;
        this.link = link;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
