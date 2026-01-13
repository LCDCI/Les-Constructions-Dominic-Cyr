package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectOverviewContent;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectOverviewContentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeature;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeatureRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImage;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImageRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class ProjectOverviewRepositoryIntegrationTest {

    @Autowired
    private ProjectOverviewContentRepository overviewContentRepository;

    @Autowired
    private ProjectFeatureRepository featureRepository;

    @Autowired
    private ProjectGalleryImageRepository galleryImageRepository;

    @BeforeEach
    void setUp() {
        galleryImageRepository.deleteAll();
        featureRepository.deleteAll();
        overviewContentRepository.deleteAll();
    }

    // ProjectOverviewContent Tests
    @Test
    void save_WhenValidOverviewContent_SavesSuccessfully() {
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-content-001")
                .heroTitle("Hero Title")
                .heroSubtitle("Hero Subtitle")
                .heroDescription("Hero Description")
                .overviewSectionTitle("Overview Title")
                .overviewSectionContent("Overview Content")
                .featuresSectionTitle("Features Title")
                .locationSectionTitle("Location Title")
                .locationDescription("Location Description")
                .locationAddress("123 Test Street")
                .locationMapEmbedUrl("https://maps.google.com/embed")
                .gallerySectionTitle("Gallery Title")
                .locationLatitude(45.5017)
                .locationLongitude(-73.5673)
                .build();

        ProjectOverviewContent saved = overviewContentRepository.save(content);

        assertNotNull(saved.getId());
        assertEquals("proj-content-001", saved.getProjectIdentifier());
        assertEquals("Hero Title", saved.getHeroTitle());
        assertEquals("Hero Subtitle", saved.getHeroSubtitle());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(45.5017, saved.getLocationLatitude());
        assertEquals(-73.5673, saved.getLocationLongitude());
    }

    @Test
    void findByProjectIdentifier_WhenExists_ReturnsContent() {
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-find-001")
                .heroTitle("Find Test")
                .heroSubtitle("Find Subtitle")
                .heroDescription("Find Description")
                .build();
        overviewContentRepository.save(content);

        Optional<ProjectOverviewContent> found = overviewContentRepository.findByProjectIdentifier("proj-find-001");

        assertTrue(found.isPresent());
        assertEquals("Find Test", found.get().getHeroTitle());
        assertEquals("proj-find-001", found.get().getProjectIdentifier());
    }

    @Test
    void findByProjectIdentifier_WhenNotExists_ReturnsEmpty() {
        Optional<ProjectOverviewContent> found = overviewContentRepository.findByProjectIdentifier("non-existent");

        assertTrue(found.isEmpty());
    }

    @Test
    void update_WhenExists_UpdatesContent() {
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-update-001")
                .heroTitle("Original Title")
                .heroSubtitle("Original Subtitle")
                .build();
        ProjectOverviewContent saved = overviewContentRepository.save(content);

        saved.setHeroTitle("Updated Title");
        saved.setHeroSubtitle("Updated Subtitle");
        saved.setLocationLatitude(46.8139);
        saved.setLocationLongitude(-71.2080);

        ProjectOverviewContent updated = overviewContentRepository.save(saved);

        assertEquals("Updated Title", updated.getHeroTitle());
        assertEquals("Updated Subtitle", updated.getHeroSubtitle());
        assertEquals(46.8139, updated.getLocationLatitude());
        assertEquals(-71.2080, updated.getLocationLongitude());
    }

    @Test
    void delete_WhenExists_RemovesContent() {
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier("proj-delete-001")
                .heroTitle("Delete Test")
                .build();
        ProjectOverviewContent saved = overviewContentRepository.save(content);

        overviewContentRepository.delete(saved);

        Optional<ProjectOverviewContent> found = overviewContentRepository.findByProjectIdentifier("proj-delete-001");
        assertTrue(found.isEmpty());
    }

    // ProjectFeature Tests
    @Test
    void save_WhenValidFeature_SavesSuccessfully() {
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier("proj-feature-001")
                .featureTitle("Feature Title")
                .featureDescription("Feature Description")
                .displayOrder(1)
                .build();

        ProjectFeature saved = featureRepository.save(feature);

        assertNotNull(saved.getId());
        assertEquals("proj-feature-001", saved.getProjectIdentifier());
        assertEquals("Feature Title", saved.getFeatureTitle());
        assertEquals("Feature Description", saved.getFeatureDescription());
        assertEquals(1, saved.getDisplayOrder());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WhenFeaturesExist_ReturnsOrderedList() {
        ProjectFeature feature3 = ProjectFeature.builder()
                .projectIdentifier("proj-order-001")
                .featureTitle("Third Feature")
                .featureDescription("Third")
                .displayOrder(3)
                .build();
        featureRepository.save(feature3);

        ProjectFeature feature1 = ProjectFeature.builder()
                .projectIdentifier("proj-order-001")
                .featureTitle("First Feature")
                .featureDescription("First")
                .displayOrder(1)
                .build();
        featureRepository.save(feature1);

        ProjectFeature feature2 = ProjectFeature.builder()
                .projectIdentifier("proj-order-001")
                .featureTitle("Second Feature")
                .featureDescription("Second")
                .displayOrder(2)
                .build();
        featureRepository.save(feature2);

        List<ProjectFeature> features = featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-order-001");

        assertEquals(3, features.size());
        assertEquals(1, features.get(0).getDisplayOrder());
        assertEquals(2, features.get(1).getDisplayOrder());
        assertEquals(3, features.get(2).getDisplayOrder());
        assertEquals("First Feature", features.get(0).getFeatureTitle());
        assertEquals("Second Feature", features.get(1).getFeatureTitle());
        assertEquals("Third Feature", features.get(2).getFeatureTitle());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WhenNoFeatures_ReturnsEmptyList() {
        List<ProjectFeature> features = featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc("non-existent");

        assertTrue(features.isEmpty());
    }

    @Test
    void delete_WhenFeatureExists_RemovesFeature() {
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier("proj-del-feature-001")
                .featureTitle("Delete Me")
                .featureDescription("To be deleted")
                .displayOrder(1)
                .build();
        ProjectFeature saved = featureRepository.save(feature);

        featureRepository.delete(saved);

        List<ProjectFeature> features = featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-del-feature-001");
        assertTrue(features.isEmpty());
    }

    @Test
    void save_MultipleFeaturesSameProject_SavesAllSuccessfully() {
        for (int i = 1; i <= 5; i++) {
            ProjectFeature feature = ProjectFeature.builder()
                    .projectIdentifier("proj-multi-001")
                    .featureTitle("Feature " + i)
                    .featureDescription("Description " + i)
                    .displayOrder(i)
                    .build();
            featureRepository.save(feature);
        }

        List<ProjectFeature> features = featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-multi-001");

        assertEquals(5, features.size());
    }

    // ProjectGalleryImage Tests
    @Test
    void save_WhenValidGalleryImage_SavesSuccessfully() {
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-gallery-001")
                .imageIdentifier("img-001")
                .imageCaption("Test Caption")
                .displayOrder(1)
                .build();

        ProjectGalleryImage saved = galleryImageRepository.save(image);

        assertNotNull(saved.getId());
        assertEquals("proj-gallery-001", saved.getProjectIdentifier());
        assertEquals("img-001", saved.getImageIdentifier());
        assertEquals("Test Caption", saved.getImageCaption());
        assertEquals(1, saved.getDisplayOrder());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WhenImagesExist_ReturnsOrderedList() {
        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .projectIdentifier("proj-img-order-001")
                .imageIdentifier("img-002")
                .imageCaption("Second Image")
                .displayOrder(2)
                .build();
        galleryImageRepository.save(image2);

        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .projectIdentifier("proj-img-order-001")
                .imageIdentifier("img-001")
                .imageCaption("First Image")
                .displayOrder(1)
                .build();
        galleryImageRepository.save(image1);

        ProjectGalleryImage image3 = ProjectGalleryImage.builder()
                .projectIdentifier("proj-img-order-001")
                .imageIdentifier("img-003")
                .imageCaption("Third Image")
                .displayOrder(3)
                .build();
        galleryImageRepository.save(image3);

        List<ProjectGalleryImage> images = galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-img-order-001");

        assertEquals(3, images.size());
        assertEquals(1, images.get(0).getDisplayOrder());
        assertEquals(2, images.get(1).getDisplayOrder());
        assertEquals(3, images.get(2).getDisplayOrder());
        assertEquals("img-001", images.get(0).getImageIdentifier());
        assertEquals("img-002", images.get(1).getImageIdentifier());
        assertEquals("img-003", images.get(2).getImageIdentifier());
    }

    @Test
    void findByProjectIdentifierOrderByDisplayOrderAsc_WhenNoImages_ReturnsEmptyList() {
        List<ProjectGalleryImage> images = galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc("non-existent");

        assertTrue(images.isEmpty());
    }

    @Test
    void delete_WhenImageExists_RemovesImage() {
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-del-img-001")
                .imageIdentifier("img-del-001")
                .imageCaption("Delete Me")
                .displayOrder(1)
                .build();
        ProjectGalleryImage saved = galleryImageRepository.save(image);

        galleryImageRepository.delete(saved);

        List<ProjectGalleryImage> images = galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-del-img-001");
        assertTrue(images.isEmpty());
    }

    @Test
    void save_MultipleImagesSameProject_SavesAllSuccessfully() {
        for (int i = 1; i <= 10; i++) {
            ProjectGalleryImage image = ProjectGalleryImage.builder()
                    .projectIdentifier("proj-multi-img-001")
                    .imageIdentifier("img-" + i)
                    .imageCaption("Caption " + i)
                    .displayOrder(i)
                    .build();
            galleryImageRepository.save(image);
        }

        List<ProjectGalleryImage> images = galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc("proj-multi-img-001");

        assertEquals(10, images.size());
    }

    @Test
    void save_ImageWithoutCaption_SavesSuccessfully() {
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-no-caption-001")
                .imageIdentifier("img-no-caption")
                .displayOrder(1)
                .build();

        ProjectGalleryImage saved = galleryImageRepository.save(image);

        assertNotNull(saved.getId());
        assertNull(saved.getImageCaption());
    }

    // Integration Tests - Multiple Entities
    @Test
    void save_ContentFeaturesAndImages_AllSavedSuccessfully() {
        String projectId = "proj-integration-001";

        // Save content
        ProjectOverviewContent content = ProjectOverviewContent.builder()
                .projectIdentifier(projectId)
                .heroTitle("Integration Test")
                .heroSubtitle("Full Integration")
                .build();
        overviewContentRepository.save(content);

        // Save features
        ProjectFeature feature1 = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Feature 1")
                .displayOrder(1)
                .build();
        ProjectFeature feature2 = ProjectFeature.builder()
                .projectIdentifier(projectId)
                .featureTitle("Feature 2")
                .displayOrder(2)
                .build();
        featureRepository.save(feature1);
        featureRepository.save(feature2);

        // Save images
        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img-1")
                .displayOrder(1)
                .build();
        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .projectIdentifier(projectId)
                .imageIdentifier("img-2")
                .displayOrder(2)
                .build();
        galleryImageRepository.save(image1);
        galleryImageRepository.save(image2);

        // Verify all saved
        Optional<ProjectOverviewContent> foundContent = overviewContentRepository.findByProjectIdentifier(projectId);
        List<ProjectFeature> foundFeatures = featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);
        List<ProjectGalleryImage> foundImages = galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(projectId);

        assertTrue(foundContent.isPresent());
        assertEquals(2, foundFeatures.size());
        assertEquals(2, foundImages.size());
    }
}