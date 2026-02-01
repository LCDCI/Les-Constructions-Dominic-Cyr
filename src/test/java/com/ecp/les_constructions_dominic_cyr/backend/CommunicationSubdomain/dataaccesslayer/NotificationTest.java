package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Notification;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    private UUID testUserId;
    private UUID testNotificationId;
    private String testTitle;
    private String testMessage;
    private NotificationCategory testCategory;
    private String testLink;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNotificationId = UUID.randomUUID();
        testTitle = "Test Notification Title";
        testMessage = "This is a test notification message";
        testCategory = NotificationCategory.GENERAL;
        testLink = "/test/link";
    }


    @Test
    void parameterizedConstructor_CreatesNotificationWithAllFields() {
        // Act
        Notification notification = new Notification(
                testUserId, testTitle, testMessage, testCategory, testLink
        );

        // Assert
        assertNotNull(notification);
        assertEquals(testUserId, notification.getUserId());
        assertEquals(testTitle, notification.getTitle());
        assertEquals(testMessage, notification.getMessage());
        assertEquals(testCategory, notification.getCategory());
        assertEquals(testLink, notification.getLink());
        assertFalse(notification.getIsRead());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void parameterizedConstructor_WithNullLink_CreatesNotification() {
        // Act
        Notification notification = new Notification(
                testUserId, testTitle, testMessage, testCategory, null
        );

        // Assert
        assertNotNull(notification);
        assertNull(notification.getLink());
        assertEquals(testTitle, notification.getTitle());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Arrange
        Notification notification = new Notification();
        LocalDateTime testCreatedAt = LocalDateTime.now();

        // Act
        notification.setNotificationId(testNotificationId);
        notification.setUserId(testUserId);
        notification.setTitle(testTitle);
        notification.setMessage(testMessage);
        notification.setCategory(testCategory);
        notification.setLink(testLink);
        notification.setIsRead(true);
        notification.setCreatedAt(testCreatedAt);

        // Assert
        assertEquals(testNotificationId, notification.getNotificationId());
        assertEquals(testUserId, notification.getUserId());
        assertEquals(testTitle, notification.getTitle());
        assertEquals(testMessage, notification.getMessage());
        assertEquals(testCategory, notification.getCategory());
        assertEquals(testLink, notification.getLink());
        assertTrue(notification.getIsRead());
        assertEquals(testCreatedAt, notification.getCreatedAt());
    }

    @Test
    void setIsRead_CanChangeReadStatus() {
        // Arrange
        Notification notification = new Notification(
                testUserId, testTitle, testMessage, testCategory, testLink
        );

        // Act & Assert
        assertFalse(notification.getIsRead());
        
        notification.setIsRead(true);
        assertTrue(notification.getIsRead());
        
        notification.setIsRead(false);
        assertFalse(notification.getIsRead());
    }

    @Test
    void parameterizedConstructor_SetsCreatedAtAndIsRead() {
        // Act
        Notification notification = new Notification(
                testUserId, testTitle, testMessage, testCategory, testLink
        );

        // Assert - Constructor sets createdAt and isRead defaults to false
        assertNotNull(notification.getCreatedAt());
        assertFalse(notification.getIsRead());
    }

    @Test
    void defaultConstructor_WithNullIsRead_CanBeSet() {
        // Arrange
        Notification notification = new Notification();
        notification.setIsRead(null);

        // Act - Set isRead explicitly
        notification.setIsRead(false);

        // Assert
        assertFalse(notification.getIsRead());
    }

    @Test
    void defaultConstructor_WithIsReadSet_PreservesValue() {
        // Arrange
        Notification notification = new Notification();
        notification.setIsRead(true);

        // Assert
        assertTrue(notification.getIsRead());
    }

    @Test
    void notification_WithDifferentCategories_StoresCorrectly() {
        // Arrange
        NotificationCategory[] categories = {
                NotificationCategory.TASK_ASSIGNED,
                NotificationCategory.PROJECT_CREATED,
                NotificationCategory.INQUIRY_RECEIVED,
                NotificationCategory.GENERAL
        };

        for (NotificationCategory category : categories) {
            // Act
            Notification notification = new Notification(
                    testUserId, testTitle, testMessage, category, testLink
            );

            // Assert
            assertEquals(category, notification.getCategory());
        }
    }

    @Test
    void notification_WithLongMessage_StoresCorrectly() {
        // Arrange
        String longMessage = "A".repeat(5000);
        Notification notification = new Notification(
                testUserId, testTitle, longMessage, testCategory, testLink
        );

        // Assert
        assertEquals(longMessage, notification.getMessage());
        assertEquals(5000, notification.getMessage().length());
    }

    @Test
    void notification_WithLongLink_StoresCorrectly() {
        // Arrange
        String longLink = "/" + "a".repeat(499);
        Notification notification = new Notification(
                testUserId, testTitle, testMessage, testCategory, longLink
        );

        // Assert
        assertEquals(longLink, notification.getLink());
    }
}
