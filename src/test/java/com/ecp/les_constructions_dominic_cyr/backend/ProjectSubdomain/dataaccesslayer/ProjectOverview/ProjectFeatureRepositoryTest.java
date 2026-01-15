package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
class ProjectFeatureRepositoryTest {

    @Autowired
    private ProjectFeatureRepository repository;

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithMultipleFeatures_ReturnsOrdered() {
        // Arrange
        String projectId = "proj-123";
        ProjectFeature feature1 = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Feature 1")
                .displayOrder(3)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectFeature feature2 = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Feature 2")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectFeature feature3 = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Feature 3")
                .displayOrder(2)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(feature1);
        repository.save(feature2);
        repository.save(feature3);

        // Act
        List<ProjectFeature> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);

        // Assert
        assertEquals(3, found.size());
        assertEquals("Feature 2", found.get(0).getFeatureTitle()); // Order 1
        assertEquals("Feature 3", found.get(1).getFeatureTitle()); // Order 2
        assertEquals("Feature 1", found.get(2).getFeatureTitle()); // Order 3
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithNoFeatures_ReturnsEmpty() {
        // Act
        List<ProjectFeature> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc("non-existent");

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithSingleFeature_ReturnsOne() {
        // Arrange
        String projectId = "proj-single";
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Single Feature")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(feature);

        // Act
        List<ProjectFeature> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);

        // Assert
        assertEquals(1, found.size());
        assertEquals("Single Feature", found.get(0).getFeatureTitle());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithDifferentProjects_ReturnsOnlyMatching() {
        // Arrange
        ProjectFeature feature1 = ProjectFeature.builder()
                .projectIdentifier("proj-1")
                .featureTitle("Project 1 Feature")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectFeature feature2 = ProjectFeature.builder()
                .projectIdentifier("proj-2")
                .featureTitle("Project 2 Feature")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(feature1);
        repository.save(feature2);

        // Act
        List<ProjectFeature> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-1");

        // Assert
        assertEquals(1, found.size());
        assertEquals("Project 1 Feature", found.get(0).getFeatureTitle());
    }

    @Test
    void save_WithNewFeature_PersistsCorrectly() {
        // Arrange
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier("proj-new")
                .featureTitle("New Feature")
                .featureDescription("New Description")
                .displayOrder(5)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        ProjectFeature saved = repository.save(feature);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("proj-new", saved.getProjectIdentifier());
        assertEquals("New Feature", saved.getFeatureTitle());
        assertEquals("New Description", saved.getFeatureDescription());
        assertEquals(5, saved.getDisplayOrder());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findAll_WithMultipleFeatures_ReturnsAll() {
        // Arrange
        repository.save(ProjectFeature.builder()
                .projectIdentifier("proj-1")
                .featureTitle("Feature 1")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build());

        repository.save(ProjectFeature.builder()
                .projectIdentifier("proj-2")
                .featureTitle("Feature 2")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build());

        // Act
        long count = repository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void delete_WithExistingFeature_RemovesFromDatabase() {
        // Arrange
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier("proj-delete")
                .featureTitle("To Delete")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();
        ProjectFeature saved = repository.save(feature);

        // Act
        repository.delete(saved);

        // Assert
        List<ProjectFeature> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-delete");
        assertTrue(found.isEmpty());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithNegativeOrder_HandlesCorrectly() {
        // Arrange
        String projectId = "proj-negative";
        ProjectFeature feature1 = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Feature -1")
                .displayOrder(-1)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectFeature feature2 = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Feature 0")
                .displayOrder(0)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(feature1);
        repository.save(feature2);

        // Act
        List<ProjectFeature> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);

        // Assert
        assertEquals(2, found.size());
        assertEquals("Feature -1", found.get(0).getFeatureTitle());
        assertEquals("Feature 0", found.get(1).getFeatureTitle());
    }
}
