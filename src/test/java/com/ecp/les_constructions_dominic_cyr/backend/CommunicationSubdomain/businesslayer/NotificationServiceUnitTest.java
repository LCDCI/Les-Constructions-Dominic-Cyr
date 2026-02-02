package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Notification;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer.NotificationMapper;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.NotificationResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID testUserId;
    private UUID testNotificationId;
    private Notification testNotification;
    private NotificationResponseModel testResponseModel;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNotificationId = UUID.randomUUID();

        testNotification = new Notification(
                testUserId,
                "Test Title",
                "Test Message",
                NotificationCategory.GENERAL,
                "/test/link"
        );
        testNotification.setNotificationId(testNotificationId);
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());

        testResponseModel = NotificationMapper.toResponseModel(testNotification);
    }

    @Test
    void getAllNotificationsByUserId_WithExistingNotifications_ReturnsList() {
        // Arrange
        Notification notification2 = new Notification(
                testUserId,
                "Another Title",
                "Another Message",
                NotificationCategory.TASK_ASSIGNED,
                "/tasks/123"
        );
        notification2.setNotificationId(UUID.randomUUID());
        notification2.setIsRead(true);

        List<Notification> notifications = Arrays.asList(testNotification, notification2);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(notifications);

        // Act
        List<NotificationResponseModel> result = notificationService.getAllNotificationsByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Title", result.get(0).getTitle());
        assertEquals("Another Title", result.get(1).getTitle());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void getAllNotificationsByUserId_WithNoNotifications_ReturnsEmptyList() {
        // Arrange
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(Collections.emptyList());

        // Act
        List<NotificationResponseModel> result = notificationService.getAllNotificationsByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void getUnreadNotificationsByUserId_WithUnreadNotifications_ReturnsOnlyUnread() {
        // Arrange
        Notification readNotification = new Notification(
                testUserId,
                "Read Title",
                "Read Message",
                NotificationCategory.GENERAL,
                null
        );
        readNotification.setNotificationId(UUID.randomUUID());
        readNotification.setIsRead(true);

        when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(testUserId, false))
                .thenReturn(Collections.singletonList(testNotification));

        // Act
        List<NotificationResponseModel> result = notificationService.getUnreadNotificationsByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
        verify(notificationRepository, times(1))
                .findByUserIdAndIsReadOrderByCreatedAtDesc(testUserId, false);
    }

    @Test
    void getUnreadCountByUserId_WithUnreadNotifications_ReturnsCount() {
        // Arrange
        when(notificationRepository.countUnreadByUserId(testUserId)).thenReturn(5L);

        // Act
        Long result = notificationService.getUnreadCountByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result);
        verify(notificationRepository, times(1)).countUnreadByUserId(testUserId);
    }

    @Test
    void getUnreadCountByUserId_WithNoUnreadNotifications_ReturnsZero() {
        // Arrange
        when(notificationRepository.countUnreadByUserId(testUserId)).thenReturn(0L);

        // Act
        Long result = notificationService.getUnreadCountByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result);
        verify(notificationRepository, times(1)).countUnreadByUserId(testUserId);
    }

    @Test
    void markAsRead_WithValidNotification_UpdatesToRead() {
        // Arrange
        when(notificationRepository.findById(testNotificationId))
                .thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.markAsRead(testNotificationId, testUserId);

        // Assert
        assertTrue(testNotification.getIsRead());
        verify(notificationRepository, times(1)).findById(testNotificationId);
        verify(notificationRepository, times(1)).save(testNotification);
    }

    @Test
    void markAsRead_WithNonExistentNotification_ThrowsException() {
        // Arrange
        when(notificationRepository.findById(testNotificationId))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.markAsRead(testNotificationId, testUserId)
        );
        assertEquals("Notification not found", exception.getMessage());
        verify(notificationRepository, times(1)).findById(testNotificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_WithNotificationBelongingToDifferentUser_ThrowsException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        when(notificationRepository.findById(testNotificationId))
                .thenReturn(Optional.of(testNotification));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.markAsRead(testNotificationId, differentUserId)
        );
        assertEquals("Notification does not belong to user", exception.getMessage());
        verify(notificationRepository, times(1)).findById(testNotificationId);
        verify(notificationRepository, never()).save(any());
    }


    @Test
    void createNotification_WithValidData_ReturnsResponseModel() {
        // Arrange
        String title = "New Notification";
        String message = "This is a test notification";
        NotificationCategory category = NotificationCategory.PROJECT_CREATED;
        String link = "/projects/123";

        Notification savedNotification = new Notification(testUserId, title, message, category, link);
        savedNotification.setNotificationId(testNotificationId);
        savedNotification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        NotificationResponseModel result = notificationService.createNotification(
                testUserId, title, message, category, link
        );

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(message, result.getMessage());
        assertEquals(category, result.getCategory());
        assertEquals(link, result.getLink());
        assertFalse(result.getIsRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_WithNullLink_CreatesSuccessfully() {
        // Arrange
        String title = "Notification Without Link";
        String message = "This notification has no link";
        NotificationCategory category = NotificationCategory.GENERAL;

        Notification savedNotification = new Notification(testUserId, title, message, category, null);
        savedNotification.setNotificationId(testNotificationId);

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        NotificationResponseModel result = notificationService.createNotification(
                testUserId, title, message, category, null
        );

        // Assert
        assertNotNull(result);
        assertNull(result.getLink());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_WithDifferentCategories_CreatesSuccessfully() {
        // Arrange
        NotificationCategory[] categories = {
                NotificationCategory.TASK_ASSIGNED,
                NotificationCategory.PROJECT_UPDATED,
                NotificationCategory.INQUIRY_RECEIVED
        };

        for (NotificationCategory category : categories) {
            Notification savedNotification = new Notification(
                    testUserId, "Title", "Message", category, null
            );
            savedNotification.setNotificationId(UUID.randomUUID());

            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

            // Act
            NotificationResponseModel result = notificationService.createNotification(
                    testUserId, "Title", "Message", category, null
            );

            // Assert
            assertNotNull(result);
            assertEquals(category, result.getCategory());
        }

        verify(notificationRepository, times(categories.length)).save(any(Notification.class));
    }
}
