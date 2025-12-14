package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.IndividualProjectResponseModel;

public interface IndividualProjectMetadataService {
    IndividualProjectResponseModel getProjectMetadata(String projectIdentifier);
}
