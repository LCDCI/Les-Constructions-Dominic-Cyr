package com.ecp.les_constructions_dominic_cyr. backend.ProjectSubdomain.BusinessLayer.Project;

import com. ecp.les_constructions_dominic_cyr.backend. ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp. les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp. les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ProjectMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils. Exception.ProjectNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa. domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java. util.ArrayList;
import java. util.List;
import java. util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public List<ProjectResponseModel> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponseModel getProjectByIdentifier(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with identifier: " + projectIdentifier));
        return projectMapper.entityToResponseModel(project);
    }

    @Override
    @Transactional
    public ProjectResponseModel createProject(ProjectRequestModel requestModel) {
        validateProjectRequestForCreate(requestModel);
        Project project = projectMapper.requestModelToEntity(requestModel);
        Project savedProject = projectRepository.save(project);
        return projectMapper. entityToResponseModel(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel requestModel) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                . orElseThrow(() -> new ProjectNotFoundException("Project not found with identifier: " + projectIdentifier));

        validateProjectRequestForUpdate(requestModel);
        projectMapper.updateEntityFromRequestModel(requestModel, project);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.entityToResponseModel(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with identifier: " + projectIdentifier));
        projectRepository. delete(project);
    }

    @Override
    public List<ProjectResponseModel> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(projectMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponseModel> getProjectsByCustomerId(String customerId) {
        return projectRepository. findByCustomerId(customerId). stream()
                .map(projectMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponseModel> getProjectsByDateRange(LocalDate startDate, LocalDate endDate) {
        return projectRepository.findByStartDateBetween(startDate, endDate).stream()
                .map(projectMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponseModel> filterProjects(ProjectStatus status, LocalDate startDate, LocalDate endDate, String customerId) {
        Specification<Project> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
            }
            if (customerId != null && !customerId.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("customerId"), customerId));
            }

            return criteriaBuilder. and(predicates.toArray(new Predicate[0]));
        };

        return projectRepository.findAll(spec).stream()
                .map(projectMapper::entityToResponseModel)
                . collect(Collectors.toList());
    }

    private void validateProjectRequestForCreate(ProjectRequestModel requestModel) {
        if (requestModel.getProjectName() == null || requestModel.getProjectName().trim().isEmpty()) {
            throw new InvalidProjectDataException("Project name cannot be empty");
        }
        if (requestModel.getStartDate() == null) {
            throw new InvalidProjectDataException("Start date cannot be null");
        }
        if (requestModel.getEndDate() != null && requestModel.getStartDate(). isAfter(requestModel.getEndDate())) {
            throw new InvalidProjectDataException("Start date cannot be after end date");
        }
        if (requestModel.getStatus() == null) {
            throw new InvalidProjectDataException("Project status cannot be null");
        }
    }

    private void validateProjectRequestForUpdate(ProjectRequestModel requestModel) {
        if (requestModel.getProjectName() != null && requestModel.getProjectName().trim(). isEmpty()) {
            throw new InvalidProjectDataException("Project name cannot be empty");
        }
        if (requestModel.getStartDate() != null && requestModel.getEndDate() != null
                && requestModel.getStartDate().isAfter(requestModel.getEndDate())) {
            throw new InvalidProjectDataException("Start date cannot be after end date");
        }
    }
}