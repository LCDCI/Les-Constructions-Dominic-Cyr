package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivingEnvironmentResponseModel {
    private String projectIdentifier;
    private String language;
    
    // Header content
    private String headerTitle;
    private String headerSubtitle;
    private String headerSubtitleLast;
    private String headerTagline;
    
    // Description
    private String descriptionText;
    
    // Proximity section
    private String proximityTitle;
    
    // Footer
    private String footerText;
    
    // Amenities list
    private List<LivingEnvironmentAmenityResponseModel> amenities;
    
    // Project colors (from project table)
    private String primaryColor;
    private String tertiaryColor;
    private String buyerColor;
}
