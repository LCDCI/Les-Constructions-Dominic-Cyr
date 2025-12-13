package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectOverviewResponseModel {
    private String projectIdentifier;
    private String projectName;
    private String status;
    private String primaryColor;
    private String tertiaryColor;
    private String buyerColor;
    private String imageIdentifier;

    private String heroTitle;
    private String heroSubtitle;
    private String heroDescription;

    private String overviewSectionTitle;
    private String overviewSectionContent;

    private String featuresSectionTitle;
    private List<ProjectFeatureResponseModel> features;

    private String locationSectionTitle;
    private String locationDescription;
    private String locationAddress;
    private String locationMapEmbedUrl;

    private String gallerySectionTitle;
    private List<ProjectGalleryImageResponseModel> galleryImages;

    private List<LotResponseModel> lots;
}