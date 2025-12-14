package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;

import java.time.LocalDate;
import java.util.List;

public interface ProjectService {
    ProjectResponseModel createProject(ProjectRequestModel requestModel);
    List<ProjectResponseModel> getAllProjects();
    ProjectResponseModel getProjectByIdentifier(String projectIdentifier);
    ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel requestModel);
    void deleteProject(String projectIdentifier);
    List<ProjectResponseModel> getProjectsByStatus(ProjectStatus status);
    List<ProjectResponseModel> getProjectsByCustomerId(String customerId);
    List<ProjectResponseModel> getProjectsByDateRange(LocalDate startDate, LocalDate endDate);
    List<ProjectResponseModel> filterProjects(ProjectStatus status, LocalDate startDate, LocalDate endDate, String customerId);

}