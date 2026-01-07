package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectOverview.ProjectOverviewServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectOverviewContent;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectOverviewContentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeature;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeatureRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImage;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImageRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ProjectOverviewMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectOverviewResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectFeatureResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectGalleryImageResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectOverviewServiceImplUnitTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectOverviewContentRepository overviewContentRepository;

    @Mock
    private ProjectFeatureRepository featureRepository;

    @Mock
    private ProjectGalleryImageRepository galleryImageRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private ProjectOverviewMapper mapper;

    @InjectMocks
    private ProjectOverviewServiceImpl projectOverviewService;

    private Project testProject;
    private ProjectOverviewContent testContent;
    private List<ProjectFeature> testFeatures;
    private List<ProjectGalleryImage> testGalleryImages;
    private List<ProjectFeatureResponseModel> testFeatureResponses;
    private List<ProjectGalleryImageResponseModel> testGalleryResponses;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setProjectIdentifier("proj-service-001");
        testProject.setProjectName("Service Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setStartDate(LocalDate.of(2025, 1, 1));
        testProject.setEndDate(LocalDate.of(2025, 12, 31));
        testProject.setPrimaryColor("#FFFFFF");
        testProject.setTertiaryColor("#000000");
        testProject.setBuyerColor("#FF0000");
        testProject.setImageIdentifier("main-img-001");
        testProject.setBuyerName("Test Buyer");
        testProject.setCustomerId("cust-001");
        testProject.setLotIdentifiers(new ArrayList<>());
        testProject.setProgressPercentage(50);

        testContent = ProjectOverviewContent.builder()
                .projectIdentifier("proj-service-001")
                .heroTitle("Hero Title")
                .heroSubtitle("Hero Subtitle")
                .heroDescription("Hero Description")
                .overviewSectionTitle("Overview Title")
                .overviewSectionContent("Overview Content")
                .featuresSectionTitle("Features Title")
                .locationSectionTitle("Location Title")
                .locationDescription("Location Description")
                .locationAddress("123 Test St")
                .locationMapEmbedUrl("https://maps.google.com/embed")
                .gallerySectionTitle("Gallery Title")
                .build();

        ProjectFeature feature1 = ProjectFeature.builder()
                .projectIdentifier("proj-service-001")
                .featureTitle("Feature 1")
                .featureDescription("Description 1")
                .displayOrder(1)
                .build();

        ProjectFeature feature2 = ProjectFeature.builder()
                .projectIdentifier("proj-service-001")
                .featureTitle("Feature 2")
                .featureDescription("Description 2")
                .displayOrder(2)
                .build();

        testFeatures = Arrays.asList(feature1, feature2);

        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .projectIdentifier("proj-service-001")
                .imageIdentifier("img-001")
                .imageCaption("Image 1")
                .displayOrder(1)
                .build();

        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .projectIdentifier("proj-service-001")
                .imageIdentifier("img-002")
                .imageCaption("Image 2")
                .displayOrder(2)
                .build();

        testGalleryImages = Arrays.asList(image1, image2);

        testFeatureResponses = Arrays.asList(
                ProjectFeatureResponseModel.builder()
                        .featureTitle("Feature 1")
                        .featureDescription("Description 1")
                        .displayOrder(1)
                        .build(),
                ProjectFeatureResponseModel.builder()
                        .featureTitle("Feature 2")
                        .featureDescription("Description 2")
                        .displayOrder(2)
                        .build()
        );

        testGalleryResponses = Arrays.asList(
                ProjectGalleryImageResponseModel.builder()
                        .imageIdentifier("img-001")
                        .imageCaption("Image 1")
                        .displayOrder(1)
                        .build(),
                ProjectGalleryImageResponseModel.builder()
                        .imageIdentifier("img-002")
                        .imageCaption("Image 2")
                        .displayOrder(2)
                        .build()
        );
    }

    @Test
    void getProjectOverview_WhenProjectExistsWithFullContent_ReturnsCompleteOverview() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testContent));
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(testFeatures);
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(testGalleryImages);
        when(mapper.featuresToResponseModels(eq(testFeatures)))
                .thenReturn(testFeatureResponses);
        when(mapper.galleryImagesToResponseModels(eq(testGalleryImages)))
                .thenReturn(testGalleryResponses);

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals("proj-service-001", result.getProjectIdentifier());
        assertEquals("Service Test Project", result.getProjectName());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals("#FFFFFF", result.getPrimaryColor());
        assertEquals("#000000", result.getTertiaryColor());
        assertEquals("#FF0000", result.getBuyerColor());
        assertEquals("main-img-001", result.getImageIdentifier());
        assertEquals("Hero Title", result.getHeroTitle());
        assertEquals("Hero Subtitle", result.getHeroSubtitle());
        assertEquals("Hero Description", result.getHeroDescription());
        assertEquals("Overview Title", result.getOverviewSectionTitle());
        assertEquals("Overview Content", result.getOverviewSectionContent());
        assertEquals("Features Title", result.getFeaturesSectionTitle());
        assertEquals("Location Title", result.getLocationSectionTitle());
        assertEquals("Location Description", result.getLocationDescription());
        assertEquals("123 Test St", result.getLocationAddress());
        assertEquals("https://maps.google.com/embed", result.getLocationMapEmbedUrl());
        assertEquals("Gallery Title", result.getGallerySectionTitle());
        assertEquals(2, result.getFeatures().size());
        assertEquals(2, result.getGalleryImages().size());

        verify(projectRepository, times(1)).findByProjectIdentifier(eq("proj-service-001"));
        verify(overviewContentRepository, times(1)).findByProjectIdentifier(eq("proj-service-001"));
        verify(featureRepository, times(1)).findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001"));
        verify(galleryImageRepository, times(1)).findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001"));
        verify(mapper, times(1)).featuresToResponseModels(eq(testFeatures));
        verify(mapper, times(1)).galleryImagesToResponseModels(eq(testGalleryImages));
    }

    @Test
    void getProjectOverview_WhenProjectExistsWithoutContent_ReturnsBasicOverview() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.empty());
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(mapper.featuresToResponseModels(any()))
                .thenReturn(Collections.emptyList());
        when(mapper.galleryImagesToResponseModels(any()))
                .thenReturn(Collections.emptyList());

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals("proj-service-001", result.getProjectIdentifier());
        assertEquals("Service Test Project", result.getProjectName());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertNull(result.getHeroTitle());
        assertNull(result.getHeroSubtitle());
        assertNull(result.getHeroDescription());
        assertNull(result.getOverviewSectionTitle());
        assertNull(result.getOverviewSectionContent());
        assertNull(result.getFeaturesSectionTitle());
        assertNull(result.getLocationSectionTitle());
        assertNull(result.getLocationDescription());
        assertNull(result.getLocationAddress());
        assertNull(result.getLocationMapEmbedUrl());
        assertNull(result.getGallerySectionTitle());
        assertTrue(result.getFeatures().isEmpty());
        assertTrue(result.getGalleryImages().isEmpty());

        verify(projectRepository, times(1)).findByProjectIdentifier(eq("proj-service-001"));
        verify(overviewContentRepository, times(1)).findByProjectIdentifier(eq("proj-service-001"));
        verify(featureRepository, times(1)).findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001"));
        verify(galleryImageRepository, times(1)).findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001"));
    }

    @Test
    void getProjectOverview_WhenProjectNotFound_ThrowsNotFoundException() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("non-existent")))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> projectOverviewService.getProjectOverview("non-existent")
        );

        assertEquals("Project not found with identifier: non-existent", exception.getMessage());

        verify(projectRepository, times(1)).findByProjectIdentifier(eq("non-existent"));
        verify(overviewContentRepository, never()).findByProjectIdentifier(any());
        verify(featureRepository, never()).findByProjectIdentifierOrderByDisplayOrderAsc(any());
        verify(galleryImageRepository, never()).findByProjectIdentifierOrderByDisplayOrderAsc(any());
    }

    @Test
    void getProjectOverview_WhenProjectExistsWithContentButNoFeaturesOrGallery_ReturnsContentOnly() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testContent));
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(mapper.featuresToResponseModels(any()))
                .thenReturn(Collections.emptyList());
        when(mapper.galleryImagesToResponseModels(any()))
                .thenReturn(Collections.emptyList());

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals("proj-service-001", result.getProjectIdentifier());
        assertEquals("Hero Title", result.getHeroTitle());
        assertEquals("Hero Subtitle", result.getHeroSubtitle());
        assertEquals("Overview Title", result.getOverviewSectionTitle());
        assertEquals("Location Title", result.getLocationSectionTitle());
        assertTrue(result.getFeatures().isEmpty());
        assertTrue(result.getGalleryImages().isEmpty());

        verify(projectRepository, times(1)).findByProjectIdentifier(eq("proj-service-001"));
        verify(overviewContentRepository, times(1)).findByProjectIdentifier(eq("proj-service-001"));
    }

    @Test
    void getProjectOverview_WhenProjectExistsWithFeaturesButNoContent_ReturnsFeaturesWithoutContent() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.empty());
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(testFeatures);
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(mapper.featuresToResponseModels(eq(testFeatures)))
                .thenReturn(testFeatureResponses);
        when(mapper.galleryImagesToResponseModels(any()))
                .thenReturn(Collections.emptyList());

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals("proj-service-001", result.getProjectIdentifier());
        assertNull(result.getHeroTitle());
        assertNull(result.getOverviewSectionTitle());
        assertEquals(2, result.getFeatures().size());
        assertTrue(result.getGalleryImages().isEmpty());

        verify(mapper, times(1)).featuresToResponseModels(eq(testFeatures));
    }

    @Test
    void getProjectOverview_WhenProjectExistsWithGalleryButNoContent_ReturnsGalleryWithoutContent() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.empty());
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(testGalleryImages);
        when(mapper.featuresToResponseModels(any()))
                .thenReturn(Collections.emptyList());
        when(mapper.galleryImagesToResponseModels(eq(testGalleryImages)))
                .thenReturn(testGalleryResponses);

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals("proj-service-001", result.getProjectIdentifier());
        assertNull(result.getHeroTitle());
        assertTrue(result.getFeatures().isEmpty());
        assertEquals(2, result.getGalleryImages().size());

        verify(mapper, times(1)).galleryImagesToResponseModels(eq(testGalleryImages));
    }

    @Test
    void getProjectOverview_WhenDifferentProjectStatus_ReturnsCorrectStatus() {
        // Arrange
        testProject.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.empty());
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(mapper.featuresToResponseModels(any()))
                .thenReturn(Collections.emptyList());
        when(mapper.galleryImagesToResponseModels(any()))
                .thenReturn(Collections.emptyList());

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void getProjectOverview_WhenMultipleFeatures_CallsMapperCorrectly() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.empty());
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(testFeatures);
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(mapper.featuresToResponseModels(eq(testFeatures)))
                .thenReturn(testFeatureResponses);
        when(mapper.galleryImagesToResponseModels(any()))
                .thenReturn(Collections.emptyList());

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getFeatures().size());
        verify(mapper, times(1)).featuresToResponseModels(eq(testFeatures));
    }

    @Test
    void getProjectOverview_WhenMultipleGalleryImages_CallsMapperCorrectly() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.of(testProject));
        when(overviewContentRepository.findByProjectIdentifier(eq("proj-service-001")))
                .thenReturn(Optional.empty());
        when(featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(Collections.emptyList());
        when(galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(eq("proj-service-001")))
                .thenReturn(testGalleryImages);
        when(mapper.featuresToResponseModels(any()))
                .thenReturn(Collections.emptyList());
        when(mapper.galleryImagesToResponseModels(eq(testGalleryImages)))
                .thenReturn(testGalleryResponses);

        // Act
        ProjectOverviewResponseModel result = projectOverviewService.getProjectOverview("proj-service-001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getGalleryImages().size());
        verify(mapper, times(1)).galleryImagesToResponseModels(eq(testGalleryImages));
    }
}