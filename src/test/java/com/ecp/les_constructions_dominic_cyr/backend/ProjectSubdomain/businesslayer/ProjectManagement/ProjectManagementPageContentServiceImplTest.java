package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectManagement;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectManagement.ProjectManagementPageContent;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectManagement.ProjectManagementPageContentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectManagementPageContentServiceImplTest {

    @Mock
    private ProjectManagementPageContentRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProjectManagementPageContentServiceImpl service;

    private ProjectManagementPageContent testContent;
    private Map<String, Object> testContentMap;
    private String testJsonContent;

    @BeforeEach
    void setUp() {
        testContentMap = new HashMap<>();
        testContentMap.put("title", "Project Management");
        testContentMap.put("description", "Manage your projects");
        testContentMap.put("sections", Map.of("header", "Welcome"));

        testJsonContent = "{\"title\":\"Project Management\",\"description\":\"Manage your projects\",\"sections\":{\"header\":\"Welcome\"}}";

        testContent = ProjectManagementPageContent.builder()
                .id(1L)
                .language("en")
                .contentJson(testJsonContent)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getContentByLanguage_WithEnglishLanguage_ReturnsContent() throws Exception {
        // Arrange
        when(repository.findByLanguage("en")).thenReturn(Optional.of(testContent));
        when(objectMapper.readValue(eq(testJsonContent), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(testContentMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage("en");

        // Assert
        assertNotNull(result);
        assertEquals("Project Management", result.get("title"));
        verify(repository).findByLanguage("en");
        verify(objectMapper).readValue(eq(testJsonContent), any(com.fasterxml.jackson.core.type.TypeReference.class));
    }

    @Test
    void getContentByLanguage_WithFrenchLanguage_ReturnsContent() throws Exception {
        // Arrange
        ProjectManagementPageContent frenchContent = ProjectManagementPageContent.builder()
                .id(2L)
                .language("fr")
                .contentJson("{\"title\":\"Gestion de Projet\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Map<String, Object> frenchMap = new HashMap<>();
        frenchMap.put("title", "Gestion de Projet");

        when(repository.findByLanguage("fr")).thenReturn(Optional.of(frenchContent));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(frenchMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage("fr");

        // Assert
        assertNotNull(result);
        assertEquals("Gestion de Projet", result.get("title"));
        verify(repository).findByLanguage("fr");
    }

    @Test
    void getContentByLanguage_WithFrenchUpperCase_NormalizesToFr() throws Exception {
        // Arrange
        ProjectManagementPageContent frenchContent = ProjectManagementPageContent.builder()
                .id(2L)
                .language("fr")
                .contentJson("{\"title\":\"Gestion\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Map<String, Object> frenchMap = new HashMap<>();
        frenchMap.put("title", "Gestion");

        when(repository.findByLanguage("fr")).thenReturn(Optional.of(frenchContent));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(frenchMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage("FR");

        // Assert
        assertNotNull(result);
        verify(repository).findByLanguage("fr");
    }

    @Test
    void getContentByLanguage_WithFrenchVariants_NormalizesToFr() throws Exception {
        // Arrange
        ProjectManagementPageContent frenchContent = ProjectManagementPageContent.builder()
                .id(2L)
                .language("fr")
                .contentJson("{\"title\":\"Gestion\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Map<String, Object> frenchMap = new HashMap<>();
        frenchMap.put("title", "Gestion");

        when(repository.findByLanguage("fr")).thenReturn(Optional.of(frenchContent));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(frenchMap);

        // Act & Assert - Test various French language codes
        service.getContentByLanguage("fr-CA");
        verify(repository, atLeastOnce()).findByLanguage("fr");

        service.getContentByLanguage("fr-FR");
        verify(repository, atLeast(2)).findByLanguage("fr");

        service.getContentByLanguage("français");
        verify(repository, atLeast(3)).findByLanguage("fr");
    }

    @Test
    void getContentByLanguage_WithNullLanguage_DefaultsToEnglish() throws Exception {
        // Arrange
        when(repository.findByLanguage("en")).thenReturn(Optional.of(testContent));
        when(objectMapper.readValue(eq(testJsonContent), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(testContentMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage(null);

        // Assert
        assertNotNull(result);
        verify(repository).findByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithEmptyLanguage_DefaultsToEnglish() throws Exception {
        // Arrange
        when(repository.findByLanguage("en")).thenReturn(Optional.of(testContent));
        when(objectMapper.readValue(eq(testJsonContent), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(testContentMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage("");

        // Assert
        assertNotNull(result);
        verify(repository).findByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithUnknownLanguage_DefaultsToEnglish() throws Exception {
        // Arrange
        when(repository.findByLanguage("en")).thenReturn(Optional.of(testContent));
        when(objectMapper.readValue(eq(testJsonContent), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(testContentMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage("de");

        // Assert
        assertNotNull(result);
        verify(repository).findByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithContentNotFound_ThrowsNotFoundException() {
        // Arrange
        when(repository.findByLanguage("en")).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            service.getContentByLanguage("en");
        });

        assertTrue(exception.getMessage().contains("Project management page content not found for language: en"));
        verify(repository).findByLanguage("en");
    }


    @Test
    void getContentByLanguage_WithComplexJsonContent_ReturnsParsedMap() throws Exception {
        // Arrange
        String complexJson = "{\"title\":\"Complex\",\"items\":[{\"id\":1,\"name\":\"Item1\"},{\"id\":2,\"name\":\"Item2\"}],\"metadata\":{\"version\":\"1.0\",\"author\":\"Admin\"}}";
        ProjectManagementPageContent complexContent = ProjectManagementPageContent.builder()
                .id(4L)
                .language("en")
                .contentJson(complexJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Map<String, Object> complexMap = new HashMap<>();
        complexMap.put("title", "Complex");
        complexMap.put("items", java.util.Arrays.asList(
                Map.of("id", 1, "name", "Item1"),
                Map.of("id", 2, "name", "Item2")
        ));
        complexMap.put("metadata", Map.of("version", "1.0", "author", "Admin"));

        when(repository.findByLanguage("en")).thenReturn(Optional.of(complexContent));
        when(objectMapper.readValue(eq(complexJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(complexMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage("en");

        // Assert
        assertNotNull(result);
        assertEquals("Complex", result.get("title"));
        assertTrue(result.containsKey("items"));
        assertTrue(result.containsKey("metadata"));
        verify(repository).findByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithWhitespaceLanguage_DefaultsToEnglish() throws Exception {
        // Arrange
        when(repository.findByLanguage("en")).thenReturn(Optional.of(testContent));
        when(objectMapper.readValue(eq(testJsonContent), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(testContentMap);

        // Act
        Map<String, Object> result = service.getContentByLanguage("   ");

        // Assert
        assertNotNull(result);
        verify(repository).findByLanguage("en");
    }
}
