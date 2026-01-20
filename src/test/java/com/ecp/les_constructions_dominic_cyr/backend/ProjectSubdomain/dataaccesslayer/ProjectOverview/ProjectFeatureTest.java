package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProjectFeatureTest {

    private ProjectFeature feature;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        feature = ProjectFeature.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .featureTitle("Modern Design")
                .featureDescription("Contemporary architectural style")
                .displayOrder(1)
                .createdAt(testTime)
                .build();
    }

    @Test
    void testBuilder_WithAllFields_SetsAllFields() {
        // Assert
        assertEquals(1L, feature.getId());
        assertEquals("proj-123", feature.getProjectIdentifier());
        assertEquals("Modern Design", feature.getFeatureTitle());
        assertEquals("Contemporary architectural style", feature.getFeatureDescription());
        assertEquals(1, feature.getDisplayOrder());
        assertEquals(testTime, feature.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor_CreatesEmptyObject() {
        // Act
        ProjectFeature empty = new ProjectFeature();

        // Assert
        assertNotNull(empty);
        assertNull(empty.getId());
        assertNull(empty.getProjectIdentifier());
        assertNull(empty.getFeatureTitle());
    }

    @Test
    void testAllArgsConstructor_SetsAllFields() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);

        // Act
        ProjectFeature allArgs = new ProjectFeature(
                2L, "proj-456", "Feature Title", "Feature Description", 5, createdAt
        );

        // Assert
        assertEquals(2L, allArgs.getId());
        assertEquals("proj-456", allArgs.getProjectIdentifier());
        assertEquals("Feature Title", allArgs.getFeatureTitle());
        assertEquals("Feature Description", allArgs.getFeatureDescription());
        assertEquals(5, allArgs.getDisplayOrder());
        assertEquals(createdAt, allArgs.getCreatedAt());
    }

    @Test
    void testSetters_ModifyFields() {
        // Act
        feature.setId(5L);
        feature.setProjectIdentifier("proj-789");
        feature.setFeatureTitle("New Feature");
        feature.setFeatureDescription("New Description");
        feature.setDisplayOrder(10);
        LocalDateTime newTime = LocalDateTime.now();
        feature.setCreatedAt(newTime);

        // Assert
        assertEquals(5L, feature.getId());
        assertEquals("proj-789", feature.getProjectIdentifier());
        assertEquals("New Feature", feature.getFeatureTitle());
        assertEquals("New Description", feature.getFeatureDescription());
        assertEquals(10, feature.getDisplayOrder());
        assertEquals(newTime, feature.getCreatedAt());
    }

    @Test
    void testGetters_ReturnCorrectValues() {
        // Assert
        assertEquals(1L, feature.getId());
        assertEquals("proj-123", feature.getProjectIdentifier());
        assertEquals("Modern Design", feature.getFeatureTitle());
        assertEquals("Contemporary architectural style", feature.getFeatureDescription());
        assertEquals(1, feature.getDisplayOrder());
        assertEquals(testTime, feature.getCreatedAt());
    }

    @Test
    void testOnCreate_SetsTimestamp() {
        // Arrange
        ProjectFeature newFeature = new ProjectFeature();

        // Act
        newFeature.onCreate();

        // Assert
        assertNotNull(newFeature.getCreatedAt());
    }

    @Test
    void testOnCreate_WithExistingCreatedAt_PreservesCreatedAt() {
        // Arrange
        LocalDateTime existingCreatedAt = LocalDateTime.of(2023, 1, 1, 0, 0);
        ProjectFeature featureWithCreatedAt = new ProjectFeature();
        featureWithCreatedAt.setCreatedAt(existingCreatedAt);

        // Act
        featureWithCreatedAt.onCreate();

        // Assert
        assertEquals(existingCreatedAt, featureWithCreatedAt.getCreatedAt());
    }

    @Test
    void testWithNullDescription_HandlesGracefully() {
        // Arrange
        ProjectFeature minimalFeature = ProjectFeature.builder()
                .projectIdentifier("proj-min")
                .featureTitle("Minimal Feature")
                .displayOrder(1)
                .build();

        // Assert
        assertNull(minimalFeature.getFeatureDescription());
        assertNotNull(minimalFeature.getFeatureTitle());
    }

    @Test
    void testWithLongDescription_HandlesLargeContent() {
        // Arrange
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longDescription.append("This is a very long feature description. ");
        }

        // Act
        feature.setFeatureDescription(longDescription.toString());

        // Assert
        assertNotNull(feature.getFeatureDescription());
        assertTrue(feature.getFeatureDescription().length() > 1000);
    }

    @Test
    void testWithZeroDisplayOrder_HandlesCorrectly() {
        // Act
        feature.setDisplayOrder(0);

        // Assert
        assertEquals(0, feature.getDisplayOrder());
    }

    @Test
    void testWithNegativeDisplayOrder_HandlesCorrectly() {
        // Act
        feature.setDisplayOrder(-1);

        // Assert
        assertEquals(-1, feature.getDisplayOrder());
    }

    @Test
    void testWithLargeDisplayOrder_HandlesCorrectly() {
        // Act
        feature.setDisplayOrder(Integer.MAX_VALUE);

        // Assert
        assertEquals(Integer.MAX_VALUE, feature.getDisplayOrder());
    }

    @Test
    void testEquals_WithSameValues_ReturnsTrue() {
        // Arrange
        ProjectFeature feature1 = ProjectFeature.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .featureTitle("Title")
                .displayOrder(1)
                .build();

        ProjectFeature feature2 = ProjectFeature.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .featureTitle("Title")
                .displayOrder(1)
                .build();

        // Assert
        assertEquals(feature1, feature2);
    }

    @Test
    void testHashCode_WithSameValues_ReturnsSameHashCode() {
        // Arrange
        ProjectFeature feature1 = ProjectFeature.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .featureTitle("Title")
                .displayOrder(1)
                .build();

        ProjectFeature feature2 = ProjectFeature.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .featureTitle("Title")
                .displayOrder(1)
                .build();

        // Assert
        assertEquals(feature1.hashCode(), feature2.hashCode());
    }

    @Test
    void testToString_ContainsAllFields() {
        // Act
        String toString = feature.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ProjectFeature"));
        assertTrue(toString.contains("proj-123"));
        assertTrue(toString.contains("Modern Design"));
    }
}
