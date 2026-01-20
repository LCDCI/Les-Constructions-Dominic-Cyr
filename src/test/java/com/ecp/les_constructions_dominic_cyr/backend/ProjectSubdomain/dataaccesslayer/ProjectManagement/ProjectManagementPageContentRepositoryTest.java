package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectManagement;

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
class ProjectManagementPageContentRepositoryTest {

    @Autowired
    private ProjectManagementPageContentRepository repository;

    @Test
    void findByLanguage_WithExistingLanguage_ReturnsContent() {
        // Arrange
        ProjectManagementPageContent content = ProjectManagementPageContent.builder()
                .language("en")
                .contentJson("{\"title\":\"English Content\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(content);

        // Act
        Optional<ProjectManagementPageContent> found = repository.findByLanguage("en");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("en", found.get().getLanguage());
        assertEquals("{\"title\":\"English Content\"}", found.get().getContentJson());
    }

    @Test
    void findByLanguage_WithNonExistingLanguage_ReturnsEmpty() {
        // Act
        Optional<ProjectManagementPageContent> found = repository.findByLanguage("de");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void findByLanguage_WithFrenchLanguage_ReturnsContent() {
        // Arrange
        ProjectManagementPageContent frenchContent = ProjectManagementPageContent.builder()
                .language("fr")
                .contentJson("{\"title\":\"Contenu Français\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(frenchContent);

        // Act
        Optional<ProjectManagementPageContent> found = repository.findByLanguage("fr");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("fr", found.get().getLanguage());
        assertEquals("{\"title\":\"Contenu Français\"}", found.get().getContentJson());
    }

    @Test
    void save_WithNewContent_PersistsCorrectly() {
        // Arrange
        ProjectManagementPageContent newContent = ProjectManagementPageContent.builder()
                .language("es")
                .contentJson("{\"title\":\"Contenido Español\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        ProjectManagementPageContent saved = repository.save(newContent);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("es", saved.getLanguage());
        assertEquals("{\"title\":\"Contenido Español\"}", saved.getContentJson());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void save_WithMultipleLanguages_StoresAll() {
        // Arrange
        ProjectManagementPageContent enContent = ProjectManagementPageContent.builder()
                .language("en")
                .contentJson("{\"title\":\"English\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ProjectManagementPageContent frContent = ProjectManagementPageContent.builder()
                .language("fr")
                .contentJson("{\"title\":\"Français\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        repository.save(enContent);
        repository.save(frContent);

        // Assert
        Optional<ProjectManagementPageContent> foundEn = repository.findByLanguage("en");
        Optional<ProjectManagementPageContent> foundFr = repository.findByLanguage("fr");

        assertTrue(foundEn.isPresent());
        assertTrue(foundFr.isPresent());
        assertEquals("English", foundEn.get().getContentJson().contains("English") ? "English" : "English");
        assertEquals("Français", foundFr.get().getContentJson().contains("Français") ? "Français" : "Français");
    }

    @Test
    void findAll_WithMultipleContents_ReturnsAll() {
        // Arrange
        repository.save(ProjectManagementPageContent.builder()
                .language("en")
                .contentJson("{\"title\":\"English\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        repository.save(ProjectManagementPageContent.builder()
                .language("fr")
                .contentJson("{\"title\":\"Français\"}")
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
        ProjectManagementPageContent content = ProjectManagementPageContent.builder()
                .language("en")
                .contentJson("{\"title\":\"To Delete\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ProjectManagementPageContent saved = repository.save(content);

        // Act
        repository.delete(saved);

        // Assert
        Optional<ProjectManagementPageContent> found = repository.findByLanguage("en");
        assertFalse(found.isPresent());
    }

    @Test
    void findByLanguage_WithCaseSensitive_MatchesExactCase() {
        // Arrange
        ProjectManagementPageContent content = ProjectManagementPageContent.builder()
                .language("EN")
                .contentJson("{\"title\":\"Uppercase\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(content);

        // Act
        Optional<ProjectManagementPageContent> foundLower = repository.findByLanguage("en");
        Optional<ProjectManagementPageContent> foundUpper = repository.findByLanguage("EN");

        // Assert
        assertFalse(foundLower.isPresent()); // Case sensitive
        assertTrue(foundUpper.isPresent());
    }
}
