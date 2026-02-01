package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.NotificationResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationResponseModelTest {

    private UUID testNotificationId;
    private String testTitle;
    private String testMessage;
    private NotificationCategory testCategory;
    private String testLink;
    private Boolean testIsRead;
    private LocalDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        testNotificationId = UUID.randomUUID();
        testTitle = "Test Notification";
        testMessage = "This is a test message";
        testCategory = NotificationCategory.GENERAL;
        testLink = "/test/link";
        testIsRead = false;
        testCreatedAt = LocalDateTime.now();
    }

    @Test
    void defaultConstructor_CreatesEmptyModel() {
        // Act
        NotificationResponseModel model = new NotificationResponseModel();

        // Assert
        assertNotNull(model);
        assertNull(model.getNotificationId());
        assertNull(model.getTitle());
        assertNull(model.getMessage());
        assertNull(model.getCategory());
        assertNull(model.getLink());
        assertNull(model.getIsRead());
        assertNull(model.getCreatedAt());
    }

    @Test
    void parameterizedConstructor_CreatesModelWithAllFields() {
        // Act
        NotificationResponseModel model = new NotificationResponseModel(
                testNotificationId, testTitle, testMessage, testCategory,
                testLink, testIsRead, testCreatedAt
        );

        // Assert
        assertNotNull(model);
        assertEquals(testNotificationId, model.getNotificationId());
        assertEquals(testTitle, model.getTitle());
        assertEquals(testMessage, model.getMessage());
        assertEquals(testCategory, model.getCategory());
        assertEquals(testLink, model.getLink());
        assertEquals(testIsRead, model.getIsRead());
        assertEquals(testCreatedAt, model.getCreatedAt());
    }

    @Test
    void parameterizedConstructor_WithNullLink_CreatesModel() {
        // Act
        NotificationResponseModel model = new NotificationResponseModel(
                testNotificationId, testTitle, testMessage, testCategory,
                null, testIsRead, testCreatedAt
        );

        // Assert
        assertNotNull(model);
        assertNull(model.getLink());
        assertEquals(testTitle, model.getTitle());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Arrange
        NotificationResponseModel model = new NotificationResponseModel();
        UUID newId = UUID.randomUUID();
        LocalDateTime newCreatedAt = LocalDateTime.now().plusDays(1);

        // Act
        model.setNotificationId(newId);
        model.setTitle("New Title");
        model.setMessage("New Message");
        model.setCategory(NotificationCategory.TASK_ASSIGNED);
        model.setLink("/new/link");
        model.setIsRead(true);
        model.setCreatedAt(newCreatedAt);

        // Assert
        assertEquals(newId, model.getNotificationId());
        assertEquals("New Title", model.getTitle());
        assertEquals("New Message", model.getMessage());
        assertEquals(NotificationCategory.TASK_ASSIGNED, model.getCategory());
        assertEquals("/new/link", model.getLink());
        assertTrue(model.getIsRead());
        assertEquals(newCreatedAt, model.getCreatedAt());
    }

    @Test
    void setIsRead_CanChangeReadStatus() {
        // Arrange
        NotificationResponseModel model = new NotificationResponseModel(
                testNotificationId, testTitle, testMessage, testCategory,
                testLink, false, testCreatedAt
        );

        // Act & Assert
        assertFalse(model.getIsRead());
        
        model.setIsRead(true);
        assertTrue(model.getIsRead());
        
        model.setIsRead(false);
        assertFalse(model.getIsRead());
    }

    @Test
    void model_WithDifferentCategories_StoresCorrectly() {
        // Arrange
        NotificationCategory[] categories = {
                NotificationCategory.TASK_ASSIGNED,
                NotificationCategory.PROJECT_CREATED,
                NotificationCategory.INQUIRY_RECEIVED
        };

        for (NotificationCategory category : categories) {
            // Act
            NotificationResponseModel model = new NotificationResponseModel(
                    testNotificationId, testTitle, testMessage, category,
                    testLink, testIsRead, testCreatedAt
            );

            // Assert
            assertEquals(category, model.getCategory());
        }
    }

    @Test
    void model_WithNullValues_HandlesCorrectly() {
        // Act
        NotificationResponseModel model = new NotificationResponseModel();
        model.setNotificationId(null);
        model.setTitle(null);
        model.setMessage(null);
        model.setCategory(null);
        model.setLink(null);
        model.setIsRead(null);
        model.setCreatedAt(null);

        // Assert
        assertNull(model.getNotificationId());
        assertNull(model.getTitle());
        assertNull(model.getMessage());
        assertNull(model.getCategory());
        assertNull(model.getLink());
        assertNull(model.getIsRead());
        assertNull(model.getCreatedAt());
    }
}
