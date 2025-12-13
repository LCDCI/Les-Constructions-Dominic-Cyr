package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectOverview;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.ProjectOverview.ProjectOverviewResponseModel;

public interface ProjectOverviewService {
    ProjectOverviewResponseModel getProjectOverview(String projectIdentifier);
}