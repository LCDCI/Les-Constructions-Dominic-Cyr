package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectManagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProjectManagementPageContentTest {

    private ProjectManagementPageContent content;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        content = ProjectManagementPageContent.builder()
                .id(1L)
                .language("en")
                .contentJson("{\"title\":\"Test\"}")
                .createdAt(testTime)
                .updatedAt(testTime)
                .build();
    }

    @Test
    void testBuilder_WithAllFields_SetsAllFields() {
        // Arrange & Act
        ProjectManagementPageContent built = ProjectManagementPageContent.builder()
                .id(2L)
                .language("fr")
                .contentJson("{\"title\":\"Test FR\"}")
                .createdAt(testTime)
                .updatedAt(testTime)
                .build();

        // Assert
        assertEquals(2L, built.getId());
        assertEquals("fr", built.getLanguage());
        assertEquals("{\"title\":\"Test FR\"}", built.getContentJson());
        assertEquals(testTime, built.getCreatedAt());
        assertEquals(testTime, built.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor_CreatesEmptyObject() {
        // Act
        ProjectManagementPageContent empty = new ProjectManagementPageContent();

        // Assert
        assertNotNull(empty);
        assertNull(empty.getId());
        assertNull(empty.getLanguage());
        assertNull(empty.getContentJson());
    }

    @Test
    void testAllArgsConstructor_SetsAllFields() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0);

        // Act
        ProjectManagementPageContent allArgs = new ProjectManagementPageContent(
                3L, "es", "{\"title\":\"Spanish\"}", createdAt, updatedAt
        );

        // Assert
        assertEquals(3L, allArgs.getId());
        assertEquals("es", allArgs.getLanguage());
        assertEquals("{\"title\":\"Spanish\"}", allArgs.getContentJson());
        assertEquals(createdAt, allArgs.getCreatedAt());
        assertEquals(updatedAt, allArgs.getUpdatedAt());
    }

    @Test
    void testSetters_ModifyFields() {
        // Act
        content.setId(5L);
        content.setLanguage("de");
        content.setContentJson("{\"title\":\"German\"}");
        LocalDateTime newTime = LocalDateTime.now();
        content.setCreatedAt(newTime);
        content.setUpdatedAt(newTime);

        // Assert
        assertEquals(5L, content.getId());
        assertEquals("de", content.getLanguage());
        assertEquals("{\"title\":\"German\"}", content.getContentJson());
        assertEquals(newTime, content.getCreatedAt());
        assertEquals(newTime, content.getUpdatedAt());
    }

    @Test
    void testGetters_ReturnCorrectValues() {
        // Assert
        assertEquals(1L, content.getId());
        assertEquals("en", content.getLanguage());
        assertEquals("{\"title\":\"Test\"}", content.getContentJson());
        assertEquals(testTime, content.getCreatedAt());
        assertEquals(testTime, content.getUpdatedAt());
    }

    @Test
    void testOnCreate_SetsTimestamps() {
        // Arrange
        ProjectManagementPageContent newContent = new ProjectManagementPageContent();

        // Act
        newContent.onCreate();

        // Assert
        assertNotNull(newContent.getCreatedAt());
        assertNotNull(newContent.getUpdatedAt());
        assertEquals(newContent.getCreatedAt(), newContent.getUpdatedAt());
    }

    @Test
    void testOnUpdate_UpdatesUpdatedAt() throws InterruptedException {
        // Arrange
        ProjectManagementPageContent updateContent = new ProjectManagementPageContent();
        updateContent.onCreate();
        LocalDateTime originalUpdatedAt = updateContent.getUpdatedAt();

        // Wait a bit to ensure time difference
        Thread.sleep(10);

        // Act
        updateContent.onUpdate();

        // Assert
        assertNotNull(updateContent.getUpdatedAt());
        assertTrue(updateContent.getUpdatedAt().isAfter(originalUpdatedAt) || 
                   updateContent.getUpdatedAt().equals(originalUpdatedAt));
        // createdAt should remain unchanged
        assertNotNull(updateContent.getCreatedAt());
    }

    @Test
    void testOnCreate_WithExistingCreatedAt_PreservesCreatedAt() {
        // Arrange
        LocalDateTime existingCreatedAt = LocalDateTime.of(2023, 1, 1, 0, 0);
        ProjectManagementPageContent contentWithCreatedAt = new ProjectManagementPageContent();
        contentWithCreatedAt.setCreatedAt(existingCreatedAt);

        // Act
        contentWithCreatedAt.onCreate();

        // Assert
        assertEquals(existingCreatedAt, contentWithCreatedAt.getCreatedAt());
        assertNotNull(contentWithCreatedAt.getUpdatedAt());
    }

    @Test
    void testEquals_WithSameValues_ReturnsTrue() {
        // Arrange
        ProjectManagementPageContent content1 = ProjectManagementPageContent.builder()
                .id(1L)
                .language("en")
                .contentJson("{\"title\":\"Test\"}")
                .createdAt(testTime)
                .updatedAt(testTime)
                .build();

        ProjectManagementPageContent content2 = ProjectManagementPageContent.builder()
                .id(1L)
                .language("en")
                .contentJson("{\"title\":\"Test\"}")
                .createdAt(testTime)
                .updatedAt(testTime)
                .build();

        // Assert
        assertEquals(content1, content2);
    }

    @Test
    void testHashCode_WithSameValues_ReturnsSameHashCode() {
        // Arrange
        ProjectManagementPageContent content1 = ProjectManagementPageContent.builder()
                .id(1L)
                .language("en")
                .contentJson("{\"title\":\"Test\"}")
                .createdAt(testTime)
                .updatedAt(testTime)
                .build();

        ProjectManagementPageContent content2 = ProjectManagementPageContent.builder()
                .id(1L)
                .language("en")
                .contentJson("{\"title\":\"Test\"}")
                .createdAt(testTime)
                .updatedAt(testTime)
                .build();

        // Assert
        assertEquals(content1.hashCode(), content2.hashCode());
    }

    @Test
    void testToString_ContainsAllFields() {
        // Act
        String toString = content.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ProjectManagementPageContent"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("language=en"));
    }

    @Test
    void testWithLongContentJson_HandlesLargeContent() {
        // Arrange
        StringBuilder longJson = new StringBuilder("{\"content\":\"");
        for (int i = 0; i < 1000; i++) {
            longJson.append("This is a very long content string. ");
        }
        longJson.append("\"}");

        // Act
        content.setContentJson(longJson.toString());

        // Assert
        assertNotNull(content.getContentJson());
        assertTrue(content.getContentJson().length() > 1000);
    }

    @Test
    void testWithNullFields_HandlesGracefully() {
        // Arrange
        ProjectManagementPageContent nullContent = new ProjectManagementPageContent();

        // Act & Assert - Should not throw exceptions
        assertNull(nullContent.getId());
        assertNull(nullContent.getLanguage());
        assertNull(nullContent.getContentJson());
        assertNull(nullContent.getCreatedAt());
        assertNull(nullContent.getUpdatedAt());
    }
}
