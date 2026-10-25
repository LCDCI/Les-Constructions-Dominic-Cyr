package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectOverview;

public record ProjectOverviewUpdateRequest(
        String heroTitle,
        String heroSubtitle,
        String heroDescription,
        String overviewSectionTitle,
        String overviewSectionContent,
        String locationSectionTitle,
        String locationDescription,
        String locationAddress,
        String imageIdentifier,
        String bannerImageIdentifier
) {
}
