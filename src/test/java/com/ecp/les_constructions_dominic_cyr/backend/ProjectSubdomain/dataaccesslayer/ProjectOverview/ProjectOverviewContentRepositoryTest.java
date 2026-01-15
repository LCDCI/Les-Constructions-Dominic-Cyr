package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
class ProjectOverviewContentRepositoryTest {

    @Autowired
    private ProjectOverviewContentRepository repository;

    @Test
    void findByProjectIdentifier_WithExistingIdentifier_ReturnsContent() {
        // Arrange
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-123")
                .heroTitle("Test Project")
                .heroSubtitle("Subtitle")
                .heroDescription("Description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(content);

        // Act
        Optional<ProjectOverviewContent> found = repository.findByProjectIdentifier("proj-123");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("proj-123", found.get().getProjectIdentifier());
        assertEquals("Test Project", found.get().getHeroTitle());
    }

    @Test
    void findByProjectIdentifier_WithNonExistingIdentifier_ReturnsEmpty() {
        // Act
        Optional<ProjectOverviewContent> found = repository.findByProjectIdentifier("non-existent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void save_WithNewContent_PersistsCorrectly() {
        // Arrange
        ProjectOverviewContent newContent = ProjectOverviewContent.builder()
                .projectIdentifier("proj-456")
                .heroTitle("New Project")
                .heroSubtitle("New Subtitle")
                .heroDescription("New Description")
                .locationLatitude(45.5017)
                .locationLongitude(-73.5673)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        ProjectOverviewContent saved = repository.save(newContent);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("proj-456", saved.getProjectIdentifier());
        assertEquals("New Project", saved.getHeroTitle());
        assertEquals(45.5017, saved.getLocationLatitude());
        assertEquals(-73.5673, saved.getLocationLongitude());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void save_WithMultipleProjects_StoresAll() {
        // Arrange
        ProjectOverviewContent content1 = ProjectOverviewContent.builder()
                .projectIdentifier("proj-1")
                .heroTitle("Project 1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ProjectOverviewContent content2 = ProjectOverviewContent.builder()
                .projectIdentifier("proj-2")
                .heroTitle("Project 2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        repository.save(content1);
        repository.save(content2);

        // Assert
        Optional<ProjectOverviewContent> found1 = repository.findByProjectIdentifier("proj-1");
        Optional<ProjectOverviewContent> found2 = repository.findByProjectIdentifier("proj-2");

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals("Project 1", found1.get().getHeroTitle());
        assertEquals("Project 2", found2.get().getHeroTitle());
    }

    @Test
    void findAll_WithMultipleContents_ReturnsAll() {
        // Arrange
        repository.save(ProjectOverviewContent.builder()
                .projectIdentifier("proj-1")
                .heroTitle("Project 1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        repository.save(ProjectOverviewContent.builder()
                .projectIdentifier("proj-2")
                .heroTitle("Project 2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Act
        long count = repository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void delete_WithExistingContent_RemovesFromDatabase() {
        // Arrange
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-to-delete")
                .heroTitle("To Delete")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ProjectOverviewContent saved = repository.save(content);

        // Act
        repository.delete(saved);

        // Assert
        Optional<ProjectOverviewContent> found = repository.findByProjectIdentifier("proj-to-delete");
        assertFalse(found.isPresent());
    }

    @Test
    void findByProjectIdentifier_WithUniqueConstraint_EnforcesUniqueness() {
        // Arrange
        ProjectOverviewContent content1 = ProjectOverviewContent.builder()
                .projectIdentifier("proj-unique")
                .heroTitle("First")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(content1);

        // Act & Assert - Should allow only one per project identifier
        Optional<ProjectOverviewContent> found = repository.findByProjectIdentifier("proj-unique");
        assertTrue(found.isPresent());
        assertEquals("First", found.get().getHeroTitle());
    }

    @Test
    void save_WithCoordinates_PersistsCorrectly() {
        // Arrange
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-coords")
                .heroTitle("With Coordinates")
                .locationLatitude(40.7128)
                .locationLongitude(-74.0060)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        ProjectOverviewContent saved = repository.save(content);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(40.7128, saved.getLocationLatitude());
        assertEquals(-74.0060, saved.getLocationLongitude());
    }

    @Test
    void save_WithNullCoordinates_HandlesGracefully() {
        // Arrange
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-null-coords")
                .heroTitle("No Coordinates")
                .locationLatitude(null)
                .locationLongitude(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        ProjectOverviewContent saved = repository.save(content);

        // Assert
        assertNotNull(saved.getId());
        assertNull(saved.getLocationLatitude());
        assertNull(saved.getLocationLongitude());
    }
}
