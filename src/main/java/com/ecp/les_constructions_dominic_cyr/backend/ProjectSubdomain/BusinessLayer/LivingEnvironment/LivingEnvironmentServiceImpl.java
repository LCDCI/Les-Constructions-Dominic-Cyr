package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LivingEnvironment;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment.LivingEnvironmentContent;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment.LivingEnvironmentContentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment.LivingEnvironmentAmenityResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment.LivingEnvironmentResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment.LivingEnvironmentUpdateRequest;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LivingEnvironmentServiceImpl implements LivingEnvironmentService {

    private final LivingEnvironmentContentRepository livingEnvironmentContentRepository;
    private final ProjectRepository projectRepository;

    @Override
    public LivingEnvironmentResponseModel getLivingEnvironment(String projectIdentifier, String language) {
        log.info("Fetching living environment for project: {} with language: {}", projectIdentifier, language);
        
        // Get project for colors
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));

        // Try to get content in requested language, fall back to English if not found
        LivingEnvironmentContent content = livingEnvironmentContentRepository
                .findByProjectIdentifierAndLanguage(projectIdentifier, language)
                .orElseGet(() -> livingEnvironmentContentRepository
                        .findByProjectIdentifierAndLanguage(projectIdentifier, "en")
                        .orElseThrow(() -> new NotFoundException(
                                "Living environment content not found for project: " + projectIdentifier)));

        log.info("Found living environment content with {} amenities", content.getAmenities().size());

        // Map amenities to response models
        List<LivingEnvironmentAmenityResponseModel> amenities = content.getAmenities().stream()
                .map(amenity -> LivingEnvironmentAmenityResponseModel.builder()
                        .key(amenity.getAmenityKey())
                        .label(amenity.getAmenityLabel())
                        .displayOrder(amenity.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        // Build and return response
        return LivingEnvironmentResponseModel.builder()
                .projectIdentifier(projectIdentifier)
                .language(content.getLanguage())
                .headerTitle(content.getHeaderTitle())
                .headerSubtitle(content.getHeaderSubtitle())
                .headerSubtitleLast(content.getHeaderSubtitleLast())
                .headerTagline(content.getHeaderTagline())
                .descriptionText(content.getDescriptionText())
                .proximityTitle(content.getProximityTitle())
                .footerText(content.getFooterText())
                .amenities(amenities)
                .primaryColor(project.getPrimaryColor())
                .tertiaryColor(project.getTertiaryColor())
                .buyerColor(project.getBuyerColor())
                .build();
    }

    @Override
    @Transactional
    public LivingEnvironmentResponseModel updateLivingEnvironment(
            String projectIdentifier,
            String language,
            LivingEnvironmentUpdateRequest request
    ) {
        projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));
        LivingEnvironmentContent content = livingEnvironmentContentRepository
                .findByProjectIdentifierAndLanguage(projectIdentifier, language)
                .orElseThrow(() -> new NotFoundException(
                        "Living environment content not found for project and language: " + projectIdentifier
                ));
        content.setHeaderTitle(request.headerTitle());
        content.setHeaderSubtitle(request.headerSubtitle());
        content.setHeaderSubtitleLast(request.headerSubtitleLast());
        content.setHeaderTagline(request.headerTagline());
        content.setDescriptionText(request.descriptionText());
        content.setProximityTitle(request.proximityTitle());
        content.setFooterText(request.footerText());
        if (request.amenities() != null) {
            content.getAmenities().forEach(amenity -> request.amenities().stream()
                    .filter(update -> update.key() != null && update.key().equals(amenity.getAmenityKey()))
                    .findFirst()
                    .ifPresent(update -> {
                        amenity.setAmenityLabel(update.label());
                        amenity.setDisplayOrder(update.displayOrder());
                    }));
        }
        livingEnvironmentContentRepository.save(content);
        return getLivingEnvironment(projectIdentifier, language);
    }
}
