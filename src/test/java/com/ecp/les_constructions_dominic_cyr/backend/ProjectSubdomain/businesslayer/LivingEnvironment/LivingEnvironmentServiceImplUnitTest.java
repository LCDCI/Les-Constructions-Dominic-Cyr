
package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer.LivingEnvironment;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LivingEnvironment.LivingEnvironmentServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment.LivingEnvironmentContent;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment.LivingEnvironmentContentRepository;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment.LivingEnvironmentAmenity;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment.LivingEnvironmentResponseModel;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LivingEnvironmentServiceImplUnitTest {
    @Mock
    private LivingEnvironmentContentRepository repository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private LivingEnvironmentServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLivingEnvironment_FullCoverage() {
        // Mock project
        Project project = new Project();
        project.setProjectIdentifier("proj-001-foresta");
        project.setPrimaryColor("#123456");
        project.setTertiaryColor("#abcdef");
        project.setBuyerColor("#fedcba");
        when(projectRepository.findByProjectIdentifier("proj-001-foresta")).thenReturn(Optional.of(project));

        // Mock amenities
        LivingEnvironmentAmenity amenity1 = new LivingEnvironmentAmenity();
        amenity1.setAmenityKey("ski");
        amenity1.setAmenityLabel("Ski Resort");
        amenity1.setDisplayOrder(1);
        LivingEnvironmentAmenity amenity2 = new LivingEnvironmentAmenity();
        amenity2.setAmenityKey("golf");
        amenity2.setAmenityLabel("Golf Course");
        amenity2.setDisplayOrder(2);
        List<LivingEnvironmentAmenity> amenities = Arrays.asList(amenity1, amenity2);

        // Mock content
        LivingEnvironmentContent content = new LivingEnvironmentContent();
        content.setProjectIdentifier("proj-001-foresta");
        content.setLanguage("en");
        content.setHeaderTitle("Test Header");
        content.setHeaderSubtitle("Test Subtitle");
        content.setHeaderSubtitleLast("Test Subtitle Last");
        content.setHeaderTagline("Tagline");
        content.setDescriptionText("Description");
        content.setProximityTitle("Proximity");
        content.setFooterText("Footer");
        content.setAmenities(amenities);
        when(repository.findByProjectIdentifierAndLanguage("proj-001-foresta", "en"))
            .thenReturn(Optional.of(content));

        LivingEnvironmentResponseModel result = service.getLivingEnvironment("proj-001-foresta", "en");
        assertNotNull(result);
        assertEquals("proj-001-foresta", result.getProjectIdentifier());
        assertEquals("en", result.getLanguage());
        assertEquals("Test Header", result.getHeaderTitle());
        assertEquals("Test Subtitle", result.getHeaderSubtitle());
        assertEquals("Test Subtitle Last", result.getHeaderSubtitleLast());
        assertEquals("Tagline", result.getHeaderTagline());
        assertEquals("Description", result.getDescriptionText());
        assertEquals("Proximity", result.getProximityTitle());
        assertEquals("Footer", result.getFooterText());
        assertEquals("#123456", result.getPrimaryColor());
        assertEquals("#abcdef", result.getTertiaryColor());
        assertEquals("#fedcba", result.getBuyerColor());
        assertEquals(2, result.getAmenities().size());
        assertEquals("ski", result.getAmenities().get(0).getKey());
        assertEquals("Ski Resort", result.getAmenities().get(0).getLabel());
        assertEquals(1, result.getAmenities().get(0).getDisplayOrder());
        assertEquals("golf", result.getAmenities().get(1).getKey());
        assertEquals("Golf Course", result.getAmenities().get(1).getLabel());
        assertEquals(2, result.getAmenities().get(1).getDisplayOrder());
    }

    @Test
    void getLivingEnvironment_FallbackToEnglish() {
        Project project = new Project();
        project.setProjectIdentifier("proj-002-fallback");
        project.setPrimaryColor("#654321");
        project.setTertiaryColor("#abcdef");
        project.setBuyerColor("#fedcba");
        when(projectRepository.findByProjectIdentifier("proj-002-fallback")).thenReturn(Optional.of(project));

        LivingEnvironmentContent enContent = new LivingEnvironmentContent();
        enContent.setProjectIdentifier("proj-002-fallback");
        enContent.setLanguage("en");
        enContent.setHeaderTitle("EN Header");
        enContent.setAmenities(Collections.emptyList());

        when(repository.findByProjectIdentifierAndLanguage("proj-002-fallback", "fr"))
            .thenReturn(Optional.empty());
        when(repository.findByProjectIdentifierAndLanguage("proj-002-fallback", "en"))
            .thenReturn(Optional.of(enContent));

        LivingEnvironmentResponseModel result = service.getLivingEnvironment("proj-002-fallback", "fr");
        assertNotNull(result);
        assertEquals("proj-002-fallback", result.getProjectIdentifier());
        assertEquals("en", result.getLanguage());
        assertEquals("EN Header", result.getHeaderTitle());
        assertEquals(0, result.getAmenities().size());
    }

    @Test
    void getLivingEnvironment_AmenityMappingLambdaCoverage() {
        Project project = new Project();
        project.setProjectIdentifier("proj-003-amenity");
        project.setPrimaryColor("#111111");
        project.setTertiaryColor("#222222");
        project.setBuyerColor("#333333");
        when(projectRepository.findByProjectIdentifier("proj-003-amenity")).thenReturn(Optional.of(project));

        LivingEnvironmentAmenity amenity = new LivingEnvironmentAmenity();
        amenity.setAmenityKey("pool");
        amenity.setAmenityLabel("Swimming Pool");
        amenity.setDisplayOrder(5);
        LivingEnvironmentContent content = new LivingEnvironmentContent();
        content.setProjectIdentifier("proj-003-amenity");
        content.setLanguage("en");
        content.setHeaderTitle("Header");
        content.setAmenities(Collections.singletonList(amenity));
        when(repository.findByProjectIdentifierAndLanguage("proj-003-amenity", "en"))
            .thenReturn(Optional.of(content));

        LivingEnvironmentResponseModel result = service.getLivingEnvironment("proj-003-amenity", "en");
        assertNotNull(result);
        assertEquals(1, result.getAmenities().size());
        assertEquals("pool", result.getAmenities().get(0).getKey());
        assertEquals("Swimming Pool", result.getAmenities().get(0).getLabel());
        assertEquals(5, result.getAmenities().get(0).getDisplayOrder());
    }

    @Test
    void getLivingEnvironment_ContentNotFoundThrows() {
        Project project = new Project();
        project.setProjectIdentifier("proj-404-content");
        when(projectRepository.findByProjectIdentifier("proj-404-content")).thenReturn(Optional.of(project));
        when(repository.findByProjectIdentifierAndLanguage("proj-404-content", "en"))
            .thenReturn(Optional.empty());
        when(repository.findByProjectIdentifierAndLanguage("proj-404-content", "fr"))
            .thenReturn(Optional.empty());
        assertThrows(com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException.class,
            () -> service.getLivingEnvironment("proj-404-content", "fr"));
    }

    @Test
    void getLivingEnvironment_Negative_NotFound() {
        when(projectRepository.findByProjectIdentifier("proj-404")).thenReturn(Optional.empty());
        assertThrows(com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException.class,
            () -> service.getLivingEnvironment("proj-404", "en"));
    }
}
