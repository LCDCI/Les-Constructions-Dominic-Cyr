package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeature;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImage;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ProjectOverviewMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectFeatureResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectGalleryImageResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectOverviewMapperTest {

    private ProjectOverviewMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProjectOverviewMapper();
    }

    // ProjectFeature Mapping Tests
    @Test
    void featureToResponseModel_WithCompleteData_MapsCorrectly() {
        // Arrange
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier("proj-001")
                .featureTitle("Smart Home Technology")
                .featureDescription("Integrated automation systems throughout the home")
                .displayOrder(1)
                .build();

        // Act
        ProjectFeatureResponseModel result = mapper.featureToResponseModel(feature);

        // Assert
        assertNotNull(result);
        assertEquals("Smart Home Technology", result.getFeatureTitle());
        assertEquals("Integrated automation systems throughout the home", result.getFeatureDescription());
        assertEquals(1, result.getDisplayOrder());
    }

    @Test
    void featureToResponseModel_WithNullDescription_MapsCorrectly() {
        // Arrange
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier("proj-001")
                .featureTitle("Energy Efficient")
                .featureDescription(null)
                .displayOrder(2)
                .build();

        // Act
        ProjectFeatureResponseModel result = mapper.featureToResponseModel(feature);

        // Assert
        assertNotNull(result);
        assertEquals("Energy Efficient", result.getFeatureTitle());
        assertNull(result.getFeatureDescription());
        assertEquals(2, result.getDisplayOrder());
    }

    @Test
    void featureToResponseModel_WithEmptyDescription_MapsCorrectly() {
        // Arrange
        ProjectFeature feature = ProjectFeature.builder()
                .projectIdentifier("proj-001")
                .featureTitle("Green Building")
                .featureDescription("")
                .displayOrder(3)
                .build();

        // Act
        ProjectFeatureResponseModel result = mapper.featureToResponseModel(feature);

        // Assert
        assertNotNull(result);
        assertEquals("Green Building", result.getFeatureTitle());
        assertEquals("", result.getFeatureDescription());
        assertEquals(3, result.getDisplayOrder());
    }

    @Test
    void featuresToResponseModels_WithMultipleFeatures_MapsAllCorrectly() {
        // Arrange
        ProjectFeature feature1 = ProjectFeature.builder()
                .featureTitle("Feature 1")
                .featureDescription("Description 1")
                .displayOrder(1)
                .build();

        ProjectFeature feature2 = ProjectFeature.builder()
                .featureTitle("Feature 2")
                .featureDescription("Description 2")
                .displayOrder(2)
                .build();

        ProjectFeature feature3 = ProjectFeature.builder()
                .featureTitle("Feature 3")
                .featureDescription("Description 3")
                .displayOrder(3)
                .build();

        List<ProjectFeature> features = Arrays.asList(feature1, feature2, feature3);

        // Act
        List<ProjectFeatureResponseModel> result = mapper.featuresToResponseModels(features);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Feature 1", result.get(0).getFeatureTitle());
        assertEquals("Description 1", result.get(0).getFeatureDescription());
        assertEquals(1, result.get(0).getDisplayOrder());
        assertEquals("Feature 2", result.get(1).getFeatureTitle());
        assertEquals("Feature 3", result.get(2).getFeatureTitle());
    }

    @Test
    void featuresToResponseModels_WithEmptyList_ReturnsEmptyList() {
        // Arrange
        List<ProjectFeature> features = Collections.emptyList();

        // Act
        List<ProjectFeatureResponseModel> result = mapper.featuresToResponseModels(features);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void featuresToResponseModels_WithSingleFeature_ReturnsListWithOneElement() {
        // Arrange
        ProjectFeature feature = ProjectFeature.builder()
                .featureTitle("Single Feature")
                .featureDescription("Single Description")
                .displayOrder(1)
                .build();

        List<ProjectFeature> features = Collections.singletonList(feature);

        // Act
        List<ProjectFeatureResponseModel> result = mapper.featuresToResponseModels(features);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Single Feature", result.get(0).getFeatureTitle());
    }

    // ProjectGalleryImage Mapping Tests
    @Test
    void galleryImageToResponseModel_WithCompleteData_MapsCorrectly() {
        // Arrange
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-001")
                .imageIdentifier("img-001")
                .imageCaption("Beautiful exterior view")
                .displayOrder(1)
                .build();

        // Act
        ProjectGalleryImageResponseModel result = mapper.galleryImageToResponseModel(image);

        // Assert
        assertNotNull(result);
        assertEquals("img-001", result.getImageIdentifier());
        assertEquals("Beautiful exterior view", result.getImageCaption());
        assertEquals(1, result.getDisplayOrder());
    }

    @Test
    void galleryImageToResponseModel_WithNullCaption_MapsCorrectly() {
        // Arrange
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-001")
                .imageIdentifier("img-002")
                .imageCaption(null)
                .displayOrder(2)
                .build();

        // Act
        ProjectGalleryImageResponseModel result = mapper.galleryImageToResponseModel(image);

        // Assert
        assertNotNull(result);
        assertEquals("img-002", result.getImageIdentifier());
        assertNull(result.getImageCaption());
        assertEquals(2, result.getDisplayOrder());
    }

    @Test
    void galleryImageToResponseModel_WithEmptyCaption_MapsCorrectly() {
        // Arrange
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .projectIdentifier("proj-001")
                .imageIdentifier("img-003")
                .imageCaption("")
                .displayOrder(3)
                .build();

        // Act
        ProjectGalleryImageResponseModel result = mapper.galleryImageToResponseModel(image);

        // Assert
        assertNotNull(result);
        assertEquals("img-003", result.getImageIdentifier());
        assertEquals("", result.getImageCaption());
        assertEquals(3, result.getDisplayOrder());
    }

    @Test
    void galleryImagesToResponseModels_WithMultipleImages_MapsAllCorrectly() {
        // Arrange
        ProjectGalleryImage image1 = ProjectGalleryImage.builder()
                .imageIdentifier("img-001")
                .imageCaption("Image 1")
                .displayOrder(1)
                .build();

        ProjectGalleryImage image2 = ProjectGalleryImage.builder()
                .imageIdentifier("img-002")
                .imageCaption("Image 2")
                .displayOrder(2)
                .build();

        ProjectGalleryImage image3 = ProjectGalleryImage.builder()
                .imageIdentifier("img-003")
                .imageCaption("Image 3")
                .displayOrder(3)
                .build();

        List<ProjectGalleryImage> images = Arrays.asList(image1, image2, image3);

        // Act
        List<ProjectGalleryImageResponseModel> result = mapper.galleryImagesToResponseModels(images);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("img-001", result.get(0).getImageIdentifier());
        assertEquals("Image 1", result.get(0).getImageCaption());
        assertEquals(1, result.get(0).getDisplayOrder());
        assertEquals("img-002", result.get(1).getImageIdentifier());
        assertEquals("img-003", result.get(2).getImageIdentifier());
    }

    @Test
    void galleryImagesToResponseModels_WithEmptyList_ReturnsEmptyList() {
        // Arrange
        List<ProjectGalleryImage> images = Collections.emptyList();

        // Act
        List<ProjectGalleryImageResponseModel> result = mapper.galleryImagesToResponseModels(images);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void galleryImagesToResponseModels_WithSingleImage_ReturnsListWithOneElement() {
        // Arrange
        ProjectGalleryImage image = ProjectGalleryImage.builder()
                .imageIdentifier("img-single")
                .imageCaption("Single Image")
                .displayOrder(1)
                .build();

        List<ProjectGalleryImage> images = Collections.singletonList(image);

        // Act
        List<ProjectGalleryImageResponseModel> result = mapper.galleryImagesToResponseModels(images);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("img-single", result.get(0).getImageIdentifier());
    }

    // Lot Mapping Tests
    @Test
    void lotToResponseModel_WithCompleteData_MapsCorrectly() {
        // Arrange
        Lot lot = new Lot(
                new LotIdentifier("lot-001"),
                "Downtown Location",
                500000.0f,
                "5000",
                "464.5",
                LotStatus.AVAILABLE
        );

        // Act
        LotResponseModel result = mapper.lotToResponseModel(lot);

        // Assert
        assertNotNull(result);
        assertEquals("lot-001", result.getLotId());
        assertEquals("Downtown Location", result.getCivicAddress());
        assertEquals(500000.0f, result.getPrice());
        assertEquals("5000", result.getDimensionsSquareFeet());
        assertEquals("464.5", result.getDimensionsSquareMeters());
        assertEquals(LotStatus.AVAILABLE, result.getLotStatus());
    }

    @Test
    void lotToResponseModel_WithNullImageIdentifier_MapsCorrectly() {
        // Arrange
        Lot lot = new Lot(
                new LotIdentifier("lot-002"),
                "Suburban Location",
                350000.0f,
                "4000",
                "371.6",
                LotStatus.PENDING
        );

        // Act
        LotResponseModel result = mapper.lotToResponseModel(lot);

        // Assert
        assertNotNull(result);
        assertEquals("lot-002", result.getLotId());
        assertEquals("Suburban Location", result.getCivicAddress());
        assertEquals(350000.0f, result.getPrice());
        assertEquals("4000", result.getDimensionsSquareFeet());
        assertEquals("371.6", result.getDimensionsSquareMeters());
        assertEquals(LotStatus.PENDING, result.getLotStatus());
    }

    @Test
    void lotToResponseModel_WithSoldStatus_MapsCorrectly() {
        // Arrange
        Lot lot = new Lot(
                new LotIdentifier("lot-003"),
                "Urban Location",
                750000.0f,
                "7200",
                "668.9",
                LotStatus.SOLD
        );

        // Act
        LotResponseModel result = mapper.lotToResponseModel(lot);

        // Assert
        assertNotNull(result);
        assertEquals("lot-003", result.getLotId());
        assertEquals(LotStatus.SOLD, result.getLotStatus());
    }

    @Test
    void lotsToSummaryResponseModels_WithMultipleLots_MapsAllCorrectly() {
        // Arrange
        Lot lot1 = new Lot(
                new LotIdentifier("lot-001"),
                "Location 1",
                100000.0f,
                "1500",
                "139.4",
                LotStatus.AVAILABLE
        );

        Lot lot2 = new Lot(
                new LotIdentifier("lot-002"),
                "Location 2",
                200000.0f,
                "2400",
                "223.0",
                LotStatus.PENDING
        );

        Lot lot3 = new Lot(
                new LotIdentifier("lot-003"),
                "Location 3",
                300000.0f,
                "3500",
                "325.2",
                LotStatus.SOLD
        );

        List<Lot> lots = Arrays.asList(lot1, lot2, lot3);

        // Act
        List<LotResponseModel> result = mapper.lotsToSummaryResponseModels(lots);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("lot-001", result.get(0).getLotId());
        assertEquals("Location 1", result.get(0).getCivicAddress());
        assertEquals(100000.0f, result.get(0).getPrice());
        assertEquals("1500", result.get(0).getDimensionsSquareFeet());
        assertEquals(LotStatus.AVAILABLE, result.get(0).getLotStatus());
        assertEquals("lot-002", result.get(1).getLotId());
        assertEquals("lot-003", result.get(2).getLotId());
    }

    @Test
    void lotsToSummaryResponseModels_WithEmptyList_ReturnsEmptyList() {
        // Arrange
        List<Lot> lots = Collections.emptyList();

        // Act
        List<LotResponseModel> result = mapper.lotsToSummaryResponseModels(lots);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void lotsToSummaryResponseModels_WithSingleLot_ReturnsListWithOneElement() {
        // Arrange
        Lot lot = new Lot(
                new LotIdentifier("lot-single"),
                "Single Location",
                450000.0f,
                "4050",
                "376.3",
                LotStatus.AVAILABLE
        );

        List<Lot> lots = Collections.singletonList(lot);

        // Act
        List<LotResponseModel> result = mapper.lotsToSummaryResponseModels(lots);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("lot-single", result.get(0).getLotId());
        assertEquals("Single Location", result.get(0).getCivicAddress());
    }

    @Test
    void lotsToSummaryResponseModels_WithDifferentStatuses_MapsAllStatusesCorrectly() {
        // Arrange
        Lot availableLot = new Lot(
                new LotIdentifier("lot-avail"),
                "Available Loc",
                100000.0f,
                "1500",
                "139.4",
                LotStatus.AVAILABLE
        );

        Lot reservedLot = new Lot(
                new LotIdentifier("lot-pending"),
                "Pending Loc",
                200000.0f,
                "2400",
                "223.0",
                LotStatus.PENDING
        );

        Lot soldLot = new Lot(
                new LotIdentifier("lot-sold"),
                "Sold Loc",
                300000.0f,
                "3500",
                "325.2",
                LotStatus.SOLD
        );

        List<Lot> lots = Arrays.asList(availableLot, reservedLot, soldLot);

        // Act
        List<LotResponseModel> result = mapper.lotsToSummaryResponseModels(lots);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(LotStatus.AVAILABLE, result.get(0).getLotStatus());
        assertEquals(LotStatus.PENDING, result.get(1).getLotStatus());
        assertEquals(LotStatus.SOLD, result.get(2).getLotStatus());
    }

    @Test
    void lotToResponseModel_WithLargePriceValue_MapsCorrectly() {
        // Arrange
        Lot lot = new Lot(
                new LotIdentifier("lot-expensive"),
                "Premium Location",
                9999999.99f,
                "20000",
                "1858.0",
                LotStatus.AVAILABLE
        );

        // Act
        LotResponseModel result = mapper.lotToResponseModel(lot);

        // Assert
        assertNotNull(result);
        assertEquals(9999999.99f, result.getPrice(), 0.01);
    }

    @Test
    void lotToResponseModel_WithSpecialCharactersInLocation_MapsCorrectly() {
        // Arrange
        Lot lot = new Lot(
                new LotIdentifier("lot-special"),
                "Location with Special Chars: #123, St. André-Est",
                500000.0f,
                "5000",
                "464.5",
                LotStatus.AVAILABLE
        );

        // Act
        LotResponseModel result = mapper.lotToResponseModel(lot);

        // Assert
        assertNotNull(result);
        assertEquals("Location with Special Chars: #123, St. André-Est", result.getCivicAddress());
    }
}