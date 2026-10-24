package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment;

import jakarta.validation.constraints.Size;

import java.util.List;

public record LivingEnvironmentUpdateRequest(
        @Size(max = 500) String headerTitle,
        @Size(max = 500) String headerSubtitle,
        @Size(max = 500) String headerSubtitleLast,
        @Size(max = 500) String headerTagline,
        @Size(max = 100_000) String descriptionText,
        @Size(max = 500) String proximityTitle,
        @Size(max = 500) String footerText,
        List<LivingEnvironmentAmenityUpdate> amenities
) {
    public record LivingEnvironmentAmenityUpdate(
            String key,
            @Size(max = 500) String label,
            Integer displayOrder
    ) {}
}
