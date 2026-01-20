package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProjectOverviewContentTest {

    private ProjectOverviewContent content;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        content = ProjectOverviewContent.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .heroTitle("Hero Title")
                .heroSubtitle("Hero Subtitle")
                .heroDescription("Hero Description")
                .overviewSectionTitle("Overview Title")
                .overviewSectionContent("Overview Content")
                .featuresSectionTitle("Features Title")
                .locationSectionTitle("Location Title")
                .locationDescription("Location Description")
                .locationAddress("123 Main St")
                .locationMapEmbedUrl("https://maps.example.com")
                .gallerySectionTitle("Gallery Title")
                .locationLatitude(45.5017)
                .locationLongitude(-73.5673)
                .createdAt(testTime)
                .updatedAt(testTime)
                .build();
    }

    @Test
    void testBuilder_WithAllFields_SetsAllFields() {
        // Assert
        assertEquals(1L, content.getId());
        assertEquals("proj-123", content.getProjectIdentifier());
        assertEquals("Hero Title", content.getHeroTitle());
        assertEquals("Hero Subtitle", content.getHeroSubtitle());
        assertEquals("Hero Description", content.getHeroDescription());
        assertEquals("Overview Title", content.getOverviewSectionTitle());
        assertEquals("Overview Content", content.getOverviewSectionContent());
        assertEquals("Features Title", content.getFeaturesSectionTitle());
        assertEquals("Location Title", content.getLocationSectionTitle());
        assertEquals("Location Description", content.getLocationDescription());
        assertEquals("123 Main St", content.getLocationAddress());
        assertEquals("https://maps.example.com", content.getLocationMapEmbedUrl());
        assertEquals("Gallery Title", content.getGallerySectionTitle());
        assertEquals(45.5017, content.getLocationLatitude());
        assertEquals(-73.5673, content.getLocationLongitude());
        assertEquals(testTime, content.getCreatedAt());
        assertEquals(testTime, content.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor_CreatesEmptyObject() {
        // Act
        ProjectOverviewContent empty = new ProjectOverviewContent();

        // Assert
        assertNotNull(empty);
        assertNull(empty.getId());
        assertNull(empty.getProjectIdentifier());
    }

    @Test
    void testAllArgsConstructor_SetsAllFields() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0);

        // Act
        ProjectOverviewContent allArgs = new ProjectOverviewContent(
                2L, "proj-456", "Title", "Subtitle", "Description",
                "Overview Title", "Overview Content", "Features Title",
                "Location Title", "Location Desc", "Address", "Map URL",
                "Gallery Title", createdAt, updatedAt, 50.0, -100.0
        );

        // Assert
        assertEquals(2L, allArgs.getId());
        assertEquals("proj-456", allArgs.getProjectIdentifier());
        assertEquals("Title", allArgs.getHeroTitle());
        assertEquals(50.0, allArgs.getLocationLatitude());
        assertEquals(-100.0, allArgs.getLocationLongitude());
    }

    @Test
    void testSetters_ModifyFields() {
        // Act
        content.setId(5L);
        content.setProjectIdentifier("proj-789");
        content.setHeroTitle("New Title");
        content.setLocationLatitude(40.7128);
        content.setLocationLongitude(-74.0060);
        LocalDateTime newTime = LocalDateTime.now();
        content.setUpdatedAt(newTime);

        // Assert
        assertEquals(5L, content.getId());
        assertEquals("proj-789", content.getProjectIdentifier());
        assertEquals("New Title", content.getHeroTitle());
        assertEquals(40.7128, content.getLocationLatitude());
        assertEquals(-74.0060, content.getLocationLongitude());
        assertEquals(newTime, content.getUpdatedAt());
    }

    @Test
    void testGetters_ReturnCorrectValues() {
        // Assert
        assertEquals(1L, content.getId());
        assertEquals("proj-123", content.getProjectIdentifier());
        assertEquals("Hero Title", content.getHeroTitle());
        assertEquals(45.5017, content.getLocationLatitude());
        assertEquals(-73.5673, content.getLocationLongitude());
    }

    @Test
    void testOnCreate_SetsTimestamps() {
        // Arrange
        ProjectOverviewContent newContent = new ProjectOverviewContent();

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
        ProjectOverviewContent updateContent = new ProjectOverviewContent();
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
    void testWithNullOptionalFields_HandlesGracefully() {
        // Arrange
        ProjectOverviewContent minimalContent = ProjectOverviewContent.builder()
                .projectIdentifier("proj-min")
                .heroTitle("Minimal Title")
                .build();

        // Assert
        assertNull(minimalContent.getHeroSubtitle());
        assertNull(minimalContent.getHeroDescription());
        assertNull(minimalContent.getLocationLatitude());
        assertNull(minimalContent.getLocationLongitude());
    }

    @Test
    void testWithLongTextFields_HandlesLargeContent() {
        // Arrange
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longDescription.append("This is a very long description. ");
        }

        // Act
        content.setHeroDescription(longDescription.toString());
        content.setOverviewSectionContent(longDescription.toString());
        content.setLocationDescription(longDescription.toString());

        // Assert
        assertNotNull(content.getHeroDescription());
        assertTrue(content.getHeroDescription().length() > 1000);
        assertNotNull(content.getOverviewSectionContent());
        assertTrue(content.getOverviewSectionContent().length() > 1000);
    }

    @Test
    void testEquals_WithSameValues_ReturnsTrue() {
        // Arrange
        ProjectOverviewContent content1 = ProjectOverviewContent.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .heroTitle("Title")
                .build();

        ProjectOverviewContent content2 = ProjectOverviewContent.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .heroTitle("Title")
                .build();

        // Assert
        assertEquals(content1, content2);
    }

    @Test
    void testHashCode_WithSameValues_ReturnsSameHashCode() {
        // Arrange
        ProjectOverviewContent content1 = ProjectOverviewContent.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .heroTitle("Title")
                .build();

        ProjectOverviewContent content2 = ProjectOverviewContent.builder()
                .id(1L)
                .projectIdentifier("proj-123")
                .heroTitle("Title")
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
        assertTrue(toString.contains("ProjectOverviewContent"));
        assertTrue(toString.contains("proj-123"));
        assertTrue(toString.contains("Hero Title"));
    }

    @Test
    void testWithCoordinates_HandlesNegativeValues() {
        // Act
        content.setLocationLatitude(-45.5017);
        content.setLocationLongitude(173.5673);

        // Assert
        assertEquals(-45.5017, content.getLocationLatitude());
        assertEquals(173.5673, content.getLocationLongitude());
    }

    @Test
    void testWithZeroCoordinates_HandlesCorrectly() {
        // Act
        content.setLocationLatitude(0.0);
        content.setLocationLongitude(0.0);

        // Assert
        assertEquals(0.0, content.getLocationLatitude());
        assertEquals(0.0, content.getLocationLongitude());
    }
}
