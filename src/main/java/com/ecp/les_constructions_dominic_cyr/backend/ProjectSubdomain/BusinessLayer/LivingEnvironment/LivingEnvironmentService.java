package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LivingEnvironment;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment.LivingEnvironmentResponseModel;

public interface LivingEnvironmentService {
    LivingEnvironmentResponseModel getLivingEnvironment(String projectIdentifier, String language);
}
