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
class ProjectGalleryImageRepositoryTest {

    @Autowired
    private ProjectGalleryImageRepository repository;

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithMultipleImages_ReturnsOrdered() {
        // Arrange
        String projectId = "proj-123";
        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img-3")
                .imageCaption("Image 3")
                .displayOrder(3)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img-1")
                .imageCaption("Image 1")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectGalleryImage image3 = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img-2")
                .imageCaption("Image 2")
                .displayOrder(2)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(image1);
        repository.save(image2);
        repository.save(image3);

        // Act
        List<ProjectGalleryImage> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);

        // Assert
        assertEquals(3, found.size());
        assertEquals("img-1", found.get(0).getImageIdentifier()); // Order 1
        assertEquals("img-2", found.get(1).getImageIdentifier()); // Order 2
        assertEquals("img-3", found.get(2).getImageIdentifier()); // Order 3
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithNoImages_ReturnsEmpty() {
        // Act
        List<ProjectGalleryImage> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc("non-existent");

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithSingleImage_ReturnsOne() {
        // Arrange
        String projectId = "proj-single";
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img-single")
                .imageCaption("Single Image")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(image);

        // Act
        List<ProjectGalleryImage> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);

        // Assert
        assertEquals(1, found.size());
        assertEquals("img-single", found.get(0).getImageIdentifier());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithDifferentProjects_ReturnsOnlyMatching() {
        // Arrange
        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .projectIdentifier("proj-1")
                .imageIdentifier("img-1")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .projectIdentifier("proj-2")
                .imageIdentifier("img-2")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(image1);
        repository.save(image2);

        // Act
        List<ProjectGalleryImage> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-1");

        // Assert
        assertEquals(1, found.size());
        assertEquals("img-1", found.get(0).getImageIdentifier());
    }

    @Test
    void save_WithNewImage_PersistsCorrectly() {
        // Arrange
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-new")
                .imageIdentifier("img-new")
                .imageCaption("New Image Caption")
                .displayOrder(5)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        ProjectGalleryImage saved = repository.save(image);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("proj-new", saved.getProjectIdentifier());
        assertEquals("img-new", saved.getImageIdentifier());
        assertEquals("New Image Caption", saved.getImageCaption());
        assertEquals(5, saved.getDisplayOrder());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findAll_WithMultipleImages_ReturnsAll() {
        // Arrange
        repository.save(ProjectGalleryImage.builder()
                .projectIdentifier("proj-1")
                .imageIdentifier("img-1")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build());

        repository.save(ProjectGalleryImage.builder()
                .projectIdentifier("proj-2")
                .imageIdentifier("img-2")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build());

        // Act
        long count = repository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void delete_WithExistingImage_RemovesFromDatabase() {
        // Arrange
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-delete")
                .imageIdentifier("img-delete")
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();
        ProjectGalleryImage saved = repository.save(image);

        // Act
        repository.delete(saved);

        // Assert
        List<ProjectGalleryImage> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-delete");
        assertTrue(found.isEmpty());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WithNegativeOrder_HandlesCorrectly() {
        // Arrange
        String projectId = "proj-negative";
        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img--1")
                .displayOrder(-1)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img-0")
                .displayOrder(0)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(image1);
        repository.save(image2);

        // Act
        List<ProjectGalleryImage> found = repository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);

        // Assert
        assertEquals(2, found.size());
        assertEquals("img--1", found.get(0).getImageIdentifier());
        assertEquals("img-0", found.get(1).getImageIdentifier());
    }

    @Test
    void save_WithNullCaption_HandlesGracefully() {
        // Arrange
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-null-caption")
                .imageIdentifier("img-null")
                .imageCaption(null)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        ProjectGalleryImage saved = repository.save(image);

        // Assert
        assertNotNull(saved.getId());
        assertNull(saved.getImageCaption());
    }
}
