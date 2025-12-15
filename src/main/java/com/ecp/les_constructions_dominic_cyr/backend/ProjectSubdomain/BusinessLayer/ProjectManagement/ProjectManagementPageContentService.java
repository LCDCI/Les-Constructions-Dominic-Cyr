package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.ProjectManagement;

import java.util.Map;

public interface ProjectManagementPageContentService {
    Map<String, Object> getContentByLanguage(String language);
}

