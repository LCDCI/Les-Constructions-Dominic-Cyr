package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeature;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImage;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectFeatureResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectGalleryImageResponseModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectOverviewMapper {

    public ProjectFeatureResponseModel featureToResponseModel(ProjectFeature feature) {
        return ProjectFeatureResponseModel.builder()
                .featureTitle(feature.getFeatureTitle())
                .featureDescription(feature.getFeatureDescription())
                .displayOrder(feature.getDisplayOrder())
                .build();
    }

    public List<ProjectFeatureResponseModel> featuresToResponseModels(List<ProjectFeature> features) {
        return features.stream()
                .map(this::featureToResponseModel)
                .collect(Collectors.toList());
    }

    public ProjectGalleryImageResponseModel galleryImageToResponseModel(ProjectGalleryImage image) {
        return ProjectGalleryImageResponseModel.builder()
                .imageIdentifier(image.getImageIdentifier())
                .imageCaption(image.getImageCaption())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    public List<ProjectGalleryImageResponseModel> galleryImagesToResponseModels(List<ProjectGalleryImage> images) {
        return images.stream()
                .map(this::galleryImageToResponseModel)
                .collect(Collectors.toList());
    }

    public LotResponseModel lotToResponseModel(Lot lot) {
        return LotResponseModel.builder()
                .lotId(lot.getLotIdentifier().getLotId().toString())
                .civicAddress(lot.getCivicAddress())
                .price(lot.getPrice())
                .dimensionsSquareFeet(lot.getDimensionsSquareFeet())
                .dimensionsSquareMeters(lot.getDimensionsSquareMeters())
                .lotStatus(lot.getLotStatus())
                .build();
    }

    public List<LotResponseModel> lotsToSummaryResponseModels(List<Lot> lots) {
        return lots.stream()
                .map(this::lotToResponseModel)
                .collect(Collectors.toList());
    }
}