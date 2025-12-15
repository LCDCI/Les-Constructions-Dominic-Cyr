package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectOverview.ProjectOverviewService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectOverviewController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectOverviewResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectFeatureResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectGalleryImageResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectOverviewController.class)
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.backend.utils.GlobalControllerExceptionHandler.class)
public class ProjectOverviewControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectOverviewService projectOverviewService;

    private ProjectOverviewResponseModel testOverviewResponseModel;

    @BeforeEach
    void setUp() {
        ProjectFeatureResponseModel feature1 = ProjectFeatureResponseModel.builder()
                .featureTitle("Modern Design")
                .featureDescription("Contemporary architectural style")
                .displayOrder(1)
                .build();

        ProjectFeatureResponseModel feature2 = ProjectFeatureResponseModel.builder()
                .featureTitle("Energy Efficient")
                .featureDescription("High-performance insulation")
                .displayOrder(2)
                .build();

        ProjectGalleryImageResponseModel image1 = ProjectGalleryImageResponseModel.builder()
                .imageIdentifier("gallery-img-001")
                .imageCaption("Front view of the property")
                .displayOrder(1)
                .build();

        ProjectGalleryImageResponseModel image2 = ProjectGalleryImageResponseModel.builder()
                .imageIdentifier("gallery-img-002")
                .imageCaption("Interior living space")
                .displayOrder(2)
                .build();

        testOverviewResponseModel = ProjectOverviewResponseModel.builder()
                .projectIdentifier("proj-overview-001")
                .projectName("Luxury Residence")
                .status("IN_PROGRESS")
                .primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                .buyerColor("#FF0000")
                .imageIdentifier("main-image-001")
                .heroTitle("Welcome to Luxury Residence")
                .heroSubtitle("Your Dream Home Awaits")
                .heroDescription("Experience modern living at its finest")
                .overviewSectionTitle("Project Overview")
                .overviewSectionContent("This is a premium residential development")
                .featuresSectionTitle("Key Features")
                .features(Arrays.asList(feature1, feature2))
                .locationSectionTitle("Location")
                .locationDescription("Prime location in the heart of the city")
                .locationAddress("123 Main Street, City, Province")
                .locationMapEmbedUrl("https://maps.google.com/embed?q=123+Main+St")
                .gallerySectionTitle("Photo Gallery")
                .galleryImages(Arrays.asList(image1, image2))
                .build();
    }

    @Test
    void getProjectOverview_WhenProjectExists_ReturnsOverview() throws Exception {
        when(projectOverviewService.getProjectOverview(eq("proj-overview-001")))
                .thenReturn(testOverviewResponseModel);

        mockMvc.perform(get("/api/v1/projects/proj-overview-001/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value("proj-overview-001"))
                .andExpect(jsonPath("$.projectName").value("Luxury Residence"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.heroTitle").value("Welcome to Luxury Residence"))
                .andExpect(jsonPath("$.heroSubtitle").value("Your Dream Home Awaits"))
                .andExpect(jsonPath("$.heroDescription").value("Experience modern living at its finest"))
                .andExpect(jsonPath("$.overviewSectionTitle").value("Project Overview"))
                .andExpect(jsonPath("$.overviewSectionContent").value("This is a premium residential development"))
                .andExpect(jsonPath("$.featuresSectionTitle").value("Key Features"))
                .andExpect(jsonPath("$.features[0].featureTitle").value("Modern Design"))
                .andExpect(jsonPath("$.features[0].featureDescription").value("Contemporary architectural style"))
                .andExpect(jsonPath("$.features[0].displayOrder").value(1))
                .andExpect(jsonPath("$.features[1].featureTitle").value("Energy Efficient"))
                .andExpect(jsonPath("$.locationSectionTitle").value("Location"))
                .andExpect(jsonPath("$.locationDescription").value("Prime location in the heart of the city"))
                .andExpect(jsonPath("$.locationAddress").value("123 Main Street, City, Province"))
                .andExpect(jsonPath("$.gallerySectionTitle").value("Photo Gallery"))
                .andExpect(jsonPath("$.galleryImages[0].imageIdentifier").value("gallery-img-001"))
                .andExpect(jsonPath("$.galleryImages[0].imageCaption").value("Front view of the property"))
                .andExpect(jsonPath("$.galleryImages[1].imageIdentifier").value("gallery-img-002"));

        verify(projectOverviewService, times(1)).getProjectOverview(eq("proj-overview-001"));
    }

    @Test
    void getProjectOverview_WhenProjectNotFound_ReturnsNotFound() throws Exception {
        when(projectOverviewService.getProjectOverview(eq("non-existent-proj")))
                .thenThrow(new NotFoundException("Project not found with identifier: non-existent-proj"));

        mockMvc.perform(get("/api/v1/projects/non-existent-proj/overview"))
                .andExpect(status().isNotFound());

        verify(projectOverviewService, times(1)).getProjectOverview(eq("non-existent-proj"));
    }

    @Test
    void getProjectOverview_WithMinimalContent_ReturnsBasicOverview() throws Exception {
        ProjectOverviewResponseModel minimalModel = ProjectOverviewResponseModel.builder()
                .projectIdentifier("proj-minimal-001")
                .projectName("Minimal Project")
                .status("PLANNED")
                .primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                .buyerColor("#0000FF")
                .imageIdentifier("minimal-image-001")
                .features(Collections.emptyList())
                .galleryImages(Collections.emptyList())
                .build();

        when(projectOverviewService.getProjectOverview(eq("proj-minimal-001")))
                .thenReturn(minimalModel);

        mockMvc.perform(get("/api/v1/projects/proj-minimal-001/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value("proj-minimal-001"))
                .andExpect(jsonPath("$.projectName").value("Minimal Project"))
                .andExpect(jsonPath("$.features").isEmpty())
                .andExpect(jsonPath("$.galleryImages").isEmpty())
                .andExpect(jsonPath("$.heroTitle").doesNotExist());

        verify(projectOverviewService, times(1)).getProjectOverview(eq("proj-minimal-001"));
    }

    @Test
    void getProjectOverview_WithSpecialCharactersInIdentifier_ReturnsOverview() throws Exception {
        String specialIdentifier = "proj-2025-test_001";
        ProjectOverviewResponseModel model = ProjectOverviewResponseModel.builder()
                .projectIdentifier(specialIdentifier)
                .projectName("Special Project")
                .status("COMPLETED")
                .primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                .buyerColor("#00FF00")
                .imageIdentifier("special-image-001")
                .features(Collections.emptyList())
                .galleryImages(Collections.emptyList())
                .build();

        when(projectOverviewService.getProjectOverview(eq(specialIdentifier)))
                .thenReturn(model);

        mockMvc.perform(get("/api/v1/projects/" + specialIdentifier + "/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectIdentifier").value(specialIdentifier))
                .andExpect(jsonPath("$.projectName").value("Special Project"));

        verify(projectOverviewService, times(1)).getProjectOverview(eq(specialIdentifier));
    }

    @Test
    void getProjectOverview_WithMultipleFeatures_ReturnsOrderedFeatures() throws Exception {
        List<ProjectFeatureResponseModel> orderedFeatures = Arrays.asList(
                ProjectFeatureResponseModel.builder()
                        .featureTitle("First Feature")
                        .featureDescription("Description 1")
                        .displayOrder(1)
                        .build(),
                ProjectFeatureResponseModel.builder()
                        .featureTitle("Second Feature")
                        .featureDescription("Description 2")
                        .displayOrder(2)
                        .build(),
                ProjectFeatureResponseModel.builder()
                        .featureTitle("Third Feature")
                        .featureDescription("Description 3")
                        .displayOrder(3)
                        .build()
        );

        ProjectOverviewResponseModel model = ProjectOverviewResponseModel.builder()
                .projectIdentifier("proj-features-001")
                .projectName("Feature Rich Project")
                .status("IN_PROGRESS")
                .primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                .buyerColor("#FF00FF")
                .imageIdentifier("features-image-001")
                .features(orderedFeatures)
                .galleryImages(Collections.emptyList())
                .build();

        when(projectOverviewService.getProjectOverview(eq("proj-features-001")))
                .thenReturn(model);

        mockMvc.perform(get("/api/v1/projects/proj-features-001/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features.length()").value(3))
                .andExpect(jsonPath("$.features[0].displayOrder").value(1))
                .andExpect(jsonPath("$.features[1].displayOrder").value(2))
                .andExpect(jsonPath("$.features[2].displayOrder").value(3));

        verify(projectOverviewService, times(1)).getProjectOverview(eq("proj-features-001"));
    }
}