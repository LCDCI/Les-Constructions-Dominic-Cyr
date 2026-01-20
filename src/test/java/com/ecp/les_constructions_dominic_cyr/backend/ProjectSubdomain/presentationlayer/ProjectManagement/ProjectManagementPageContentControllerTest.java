package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectManagement;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectManagement.ProjectManagementPageContentService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.GlobalControllerExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectManagementPageContentController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(GlobalControllerExceptionHandler.class)
class ProjectManagementPageContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectManagementPageContentService service;

    private Map<String, Object> testContent;

    @BeforeEach
    void setUp() {
        testContent = new HashMap<>();
        testContent.put("title", "Project Management");
        testContent.put("description", "Manage your projects");
        testContent.put("sections", Map.of("header", "Welcome", "footer", "Goodbye"));
    }

    @Test
    void getContentByLanguage_WithValidLanguage_ReturnsContent() throws Exception {
        // Arrange
        when(service.getContentByLanguage("en")).thenReturn(testContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Project Management"))
                .andExpect(jsonPath("$.description").value("Manage your projects"))
                .andExpect(jsonPath("$.sections.header").value("Welcome"));

        verify(service).getContentByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithFrenchLanguage_ReturnsContent() throws Exception {
        // Arrange
        Map<String, Object> frenchContent = new HashMap<>();
        frenchContent.put("title", "Gestion de Projet");
        frenchContent.put("description", "Gérez vos projets");

        when(service.getContentByLanguage("fr")).thenReturn(frenchContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/fr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Gestion de Projet"))
                .andExpect(jsonPath("$.description").value("Gérez vos projets"));

        verify(service).getContentByLanguage("fr");
    }

    @Test
    void getContentByLanguage_WithUpperCaseLanguage_ReturnsContent() throws Exception {
        // Arrange
        when(service.getContentByLanguage("FR")).thenReturn(testContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/FR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Project Management"));

        verify(service).getContentByLanguage("FR");
    }

    @Test
    void getContentByLanguage_WithLanguageWithHyphen_ReturnsContent() throws Exception {
        // Arrange
        when(service.getContentByLanguage("fr-CA")).thenReturn(testContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/fr-CA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Project Management"));

        verify(service).getContentByLanguage("fr-CA");
    }

    @Test
    void getContentDefault_WithoutLanguage_ReturnsEnglishContent() throws Exception {
        // Arrange
        when(service.getContentByLanguage("en")).thenReturn(testContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Project Management"))
                .andExpect(jsonPath("$.description").value("Manage your projects"));

        verify(service).getContentByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithComplexContent_ReturnsAllFields() throws Exception {
        // Arrange
        Map<String, Object> complexContent = new HashMap<>();
        complexContent.put("title", "Complex Title");
        complexContent.put("items", java.util.Arrays.asList("item1", "item2", "item3"));
        complexContent.put("metadata", Map.of("version", "1.0", "author", "Admin"));
        complexContent.put("nested", Map.of("level1", Map.of("level2", "value")));

        when(service.getContentByLanguage("en")).thenReturn(complexContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Complex Title"))
                .andExpect(jsonPath("$.items[0]").value("item1"))
                .andExpect(jsonPath("$.items[1]").value("item2"))
                .andExpect(jsonPath("$.items[2]").value("item3"))
                .andExpect(jsonPath("$.metadata.version").value("1.0"))
                .andExpect(jsonPath("$.metadata.author").value("Admin"))
                .andExpect(jsonPath("$.nested.level1.level2").value("value"));

        verify(service).getContentByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithEmptyContent_ReturnsEmptyMap() throws Exception {
        // Arrange
        Map<String, Object> emptyContent = new HashMap<>();
        when(service.getContentByLanguage("en")).thenReturn(emptyContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(service).getContentByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithServiceException_ReturnsError() throws Exception {
        // Arrange
        when(service.getContentByLanguage("en"))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/en"))
                .andExpect(status().isInternalServerError());

        verify(service).getContentByLanguage("en");
    }

    @Test
    void getContentByLanguage_WithNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(service.getContentByLanguage("de"))
                .thenThrow(new com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException("Content not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/de"))
                .andExpect(status().isNotFound());

        verify(service).getContentByLanguage("de");
    }

    @Test
    void getContentByLanguage_WithSpecialCharacters_HandlesCorrectly() throws Exception {
        // Arrange
        Map<String, Object> specialContent = new HashMap<>();
        specialContent.put("title", "Test & Special Characters: < > \" '");
        when(service.getContentByLanguage("en")).thenReturn(specialContent);

        // Act & Assert
        mockMvc.perform(get("/api/v1/project-management/content/en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test & Special Characters: < > \" '"));

        verify(service).getContentByLanguage("en");
    }

    @Test
    void getContentDefault_WithMultipleCalls_CallsServiceEachTime() throws Exception {
        // Arrange
        when(service.getContentByLanguage("en")).thenReturn(testContent);

        // Act
        mockMvc.perform(get("/api/v1/project-management/content"));
        mockMvc.perform(get("/api/v1/project-management/content"));
        mockMvc.perform(get("/api/v1/project-management/content"));

        // Assert
        verify(service, times(3)).getContentByLanguage("en");
    }
}
