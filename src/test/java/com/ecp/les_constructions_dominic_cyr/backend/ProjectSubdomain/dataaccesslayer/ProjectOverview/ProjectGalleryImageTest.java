package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProjectGalleryImageTest {

    private ProjectGalleryImage image;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        image = ProjectGalleryImage.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .imageIdentifier("img-456")
                .imageCaption("Beautiful view")
                .displayOrder(1)
                .createdAt(testTime)
                .build();
    }

    @Test
    void testBuilder_WithAllFields_SetsAllFields() {
        // Assert
        assertEquals(1L, image.getId());
        assertEquals("proj-123", image.getProjectIdentifier());
        assertEquals("img-456", image.getImageIdentifier());
        assertEquals("Beautiful view", image.getImageCaption());
        assertEquals(1, image.getDisplayOrder());
        assertEquals(testTime, image.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor_CreatesEmptyObject() {
        // Act
        ProjectGalleryImage empty = new ProjectGalleryImage();

        // Assert
        assertNotNull(empty);
        assertNull(empty.getId());
        assertNull(empty.getProjectIdentifier());
        assertNull(empty.getImageIdentifier());
    }

    @Test
    void testAllArgsConstructor_SetsAllFields() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);

        // Act
        ProjectGalleryImage allArgs = new ProjectGalleryImage(
                2L, "proj-789", "img-999", "Caption", 3, createdAt
        );

        // Assert
        assertEquals(2L, allArgs.getId());
        assertEquals("proj-789", allArgs.getProjectIdentifier());
        assertEquals("img-999", allArgs.getImageIdentifier());
        assertEquals("Caption", allArgs.getImageCaption());
        assertEquals(3, allArgs.getDisplayOrder());
        assertEquals(createdAt, allArgs.getCreatedAt());
    }

    @Test
    void testSetters_ModifyFields() {
        // Act
        image.setId(5L);
        image.setProjectIdentifier("proj-999");
        image.setImageIdentifier("img-888");
        image.setImageCaption("New Caption");
        image.setDisplayOrder(10);
        LocalDateTime newTime = LocalDateTime.now();
        image.setCreatedAt(newTime);

        // Assert
        assertEquals(5L, image.getId());
        assertEquals("proj-999", image.getProjectIdentifier());
        assertEquals("img-888", image.getImageIdentifier());
        assertEquals("New Caption", image.getImageCaption());
        assertEquals(10, image.getDisplayOrder());
        assertEquals(newTime, image.getCreatedAt());
    }

    @Test
    void testGetters_ReturnCorrectValues() {
        // Assert
        assertEquals(1L, image.getId());
        assertEquals("proj-123", image.getProjectIdentifier());
        assertEquals("img-456", image.getImageIdentifier());
        assertEquals("Beautiful view", image.getImageCaption());
        assertEquals(1, image.getDisplayOrder());
        assertEquals(testTime, image.getCreatedAt());
    }

    @Test
    void testOnCreate_SetsTimestamp() {
        // Arrange
        ProjectGalleryImage newImage = new ProjectGalleryImage();

        // Act
        newImage.onCreate();

        // Assert
        assertNotNull(newImage.getCreatedAt());
    }

    @Test
    void testOnCreate_WithExistingCreatedAt_PreservesCreatedAt() {
        // Arrange
        LocalDateTime existingCreatedAt = LocalDateTime.of(2023, 1, 1, 0, 0);
        ProjectGalleryImage imageWithCreatedAt = new ProjectGalleryImage();
        imageWithCreatedAt.setCreatedAt(existingCreatedAt);

        // Act
        imageWithCreatedAt.onCreate();

        // Assert
        assertEquals(existingCreatedAt, imageWithCreatedAt.getCreatedAt());
    }

    @Test
    void testWithNullCaption_HandlesGracefully() {
        // Arrange
        ProjectGalleryImage minimalImage = ProjectGalleryImage.builder()
                .projectIdentifier("proj-min")
                .imageIdentifier("img-min")
                .displayOrder(1)
                .build();

        // Assert
        assertNull(minimalImage.getImageCaption());
        assertNotNull(minimalImage.getImageIdentifier());
    }

    @Test
    void testWithLongCaption_HandlesLargeContent() {
        // Arrange
        StringBuilder longCaption = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longCaption.append("This is a very long image caption. ");
        }

        // Act
        image.setImageCaption(longCaption.toString());

        // Assert
        assertNotNull(image.getImageCaption());
        assertTrue(image.getImageCaption().length() > 500);
    }

    @Test
    void testWithZeroDisplayOrder_HandlesCorrectly() {
        // Act
        image.setDisplayOrder(0);

        // Assert
        assertEquals(0, image.getDisplayOrder());
    }

    @Test
    void testWithNegativeDisplayOrder_HandlesCorrectly() {
        // Act
        image.setDisplayOrder(-1);

        // Assert
        assertEquals(-1, image.getDisplayOrder());
    }

    @Test
    void testWithLargeDisplayOrder_HandlesCorrectly() {
        // Act
        image.setDisplayOrder(Integer.MAX_VALUE);

        // Assert
        assertEquals(Integer.MAX_VALUE, image.getDisplayOrder());
    }

    @Test
    void testEquals_WithSameValues_ReturnsTrue() {
        // Arrange
        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .imageIdentifier("img-456")
                .displayOrder(1)
                .build();

        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .imageIdentifier("img-456")
                .displayOrder(1)
                .build();

        // Assert
        assertEquals(image1, image2);
    }

    @Test
    void testHashCode_WithSameValues_ReturnsSameHashCode() {
        // Arrange
        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .imageIdentifier("img-456")
                .displayOrder(1)
                .build();

        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .imageIdentifier("img-456")
                .displayOrder(1)
                .build();

        // Assert
        assertEquals(image1.hashCode(), image2.hashCode());
    }

    @Test
    void testToString_ContainsAllFields() {
        // Act
        String toString = image.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ProjectGalleryImage"));
        assertTrue(toString.contains("proj-123"));
        assertTrue(toString.contains("img-456"));
    }
}
