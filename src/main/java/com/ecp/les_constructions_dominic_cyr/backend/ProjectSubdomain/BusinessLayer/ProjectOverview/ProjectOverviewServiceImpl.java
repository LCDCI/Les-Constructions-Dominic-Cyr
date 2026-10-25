package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectOverview;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectOverviewContent;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectOverviewContentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeature;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectFeatureRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImage;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview.ProjectGalleryImageRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ProjectOverviewMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectOverviewResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectOverviewServiceImpl implements ProjectOverviewService {

    private final ProjectRepository projectRepository;
    private final ProjectOverviewContentRepository overviewContentRepository;
    private final ProjectFeatureRepository featureRepository;
    private final ProjectGalleryImageRepository galleryImageRepository;
    private final ProjectOverviewMapper mapper;

    @Override
    public ProjectOverviewResponseModel getProjectOverview(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));

        ProjectOverviewContent content = overviewContentRepository.findByProjectIdentifier(projectIdentifier)
                .orElse(null);

        List<ProjectFeature> features = featureRepository.findByProjectIdentifierOrderByDisplayOrderAsc(projectIdentifier);

        List<ProjectGalleryImage> galleryImages = galleryImageRepository.findByProjectIdentifierOrderByDisplayOrderAsc(projectIdentifier);

        ProjectOverviewResponseModel.ProjectOverviewResponseModelBuilder builder = ProjectOverviewResponseModel.builder()
                .projectIdentifier(project.getProjectIdentifier())
                .projectName(project.getProjectName())
                .status(project.getStatus().name())
                .primaryColor(project.getPrimaryColor())
                .tertiaryColor(project.getTertiaryColor())
                .buyerColor(project.getBuyerColor())
                .imageIdentifier(project.getImageIdentifier())
                .bannerImageIdentifier(project.getBannerImageIdentifier())
                .features(mapper.featuresToResponseModels(features))
                .galleryImages(mapper.galleryImagesToResponseModels(galleryImages));

        if (content != null) {
            builder.heroTitle(content.getHeroTitle())
                    .heroSubtitle(content.getHeroSubtitle())
                    .heroDescription(content.getHeroDescription())
                    .overviewSectionTitle(content.getOverviewSectionTitle())
                    .overviewSectionContent(content.getOverviewSectionContent())
                    .featuresSectionTitle(content.getFeaturesSectionTitle())
                    .locationSectionTitle(content.getLocationSectionTitle())
                    .locationDescription(content.getLocationDescription())
                    .locationAddress(content.getLocationAddress())
                    .locationMapEmbedUrl(content.getLocationMapEmbedUrl())
                    .locationLatitude(content.getLocationLatitude())
                    .locationLongitude(content.getLocationLongitude())
                    .gallerySectionTitle(content.getGallerySectionTitle());
        }

        return builder.build();
    }

    @Override
    @Transactional
    public ProjectOverviewResponseModel updateProjectOverview(
            String projectIdentifier,
            ProjectOverviewUpdateRequest request
    ) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new NotFoundException("Project not found with identifier: " + projectIdentifier));
        ProjectOverviewContent content = overviewContentRepository.findByProjectIdentifier(projectIdentifier)
                .orElseGet(() -> ProjectOverviewContent.builder()
                        .projectIdentifier(projectIdentifier)
                        .heroTitle(project.getProjectName())
                        .build());

        if (request.heroTitle() != null) content.setHeroTitle(request.heroTitle());
        if (request.heroSubtitle() != null) content.setHeroSubtitle(request.heroSubtitle());
        if (request.heroDescription() != null) content.setHeroDescription(request.heroDescription());
        if (request.overviewSectionTitle() != null) content.setOverviewSectionTitle(request.overviewSectionTitle());
        if (request.overviewSectionContent() != null) content.setOverviewSectionContent(request.overviewSectionContent());
        if (request.locationSectionTitle() != null) content.setLocationSectionTitle(request.locationSectionTitle());
        if (request.locationDescription() != null) content.setLocationDescription(request.locationDescription());
        if (request.locationAddress() != null) content.setLocationAddress(request.locationAddress());
        if (request.imageIdentifier() != null) project.setImageIdentifier(request.imageIdentifier());
        if (request.bannerImageIdentifier() != null) {
            project.setBannerImageIdentifier(request.bannerImageIdentifier());
        }

        overviewContentRepository.save(content);
        projectRepository.save(project);
        return getProjectOverview(projectIdentifier);
    }
}