package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ProjectMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ProjectNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final LotRepository lotRepository;
    private final FileServiceClient fileServiceClient;

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
    public ProjectResponseModel updateProject(String projectIdentifier, ProjectRequestModel requestModel) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with identifier: " + projectIdentifier));

        validateProjectRequestForUpdate(requestModel);
        
        // Validate all lots exist if lotIdentifiers are being updated
        if (requestModel.getLotIdentifiers() != null && !requestModel.getLotIdentifiers().isEmpty()) {
            validateLotsExist(requestModel.getLotIdentifiers());
        }
        
        // Validate image exists in files service if imageIdentifier is being updated (async)
        if (isValidImageIdentifier(requestModel.getImageIdentifier())) {
            validateImageExists(requestModel.getImageIdentifier());
        }
        
        projectMapper.updateEntityFromRequestModel(requestModel, project);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.entityToResponseModel(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponseModel createProject(ProjectRequestModel requestModel) {
        validateProjectRequestForCreate(requestModel);
        
        // Validate all lots exist if lotIdentifiers are provided
        if (requestModel.getLotIdentifiers() != null && !requestModel.getLotIdentifiers().isEmpty()) {
            validateLotsExist(requestModel.getLotIdentifiers());
        }
        
        // Validate image exists in files service if imageIdentifier is provided (async)
        if (isValidImageIdentifier(requestModel.getImageIdentifier())) {
            validateImageExists(requestModel.getImageIdentifier());
        }
        
        Project project = projectMapper.requestModelToEntity(requestModel);
        Project savedProject = projectRepository.save(project);
        return projectMapper.entityToResponseModel(savedProject);
    }

    @Override
    @Transactional
    public void deleteProject(String projectIdentifier) {
        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with identifier: " + projectIdentifier));
        projectRepository.delete(project);
    }

    @Override
    public List<ProjectResponseModel> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(projectMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponseModel> getProjectsByCustomerId(String customerId) {
        return projectRepository.findByCustomerId(customerId).stream()
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

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return projectRepository.findAll(spec).stream()
                .map(projectMapper::entityToResponseModel)
                .collect(Collectors.toList());
    }

    private void validateProjectRequestForCreate(ProjectRequestModel requestModel) {
        if (requestModel.getProjectName() == null || requestModel.getProjectName().trim().isEmpty()) {
            throw new InvalidProjectDataException("Project name cannot be empty");
        }
        if (requestModel.getStartDate() == null) {
            throw new InvalidProjectDataException("Start date cannot be null");
        }
        if (requestModel.getEndDate() != null && requestModel.getStartDate().isAfter(requestModel.getEndDate())) {
            throw new InvalidProjectDataException("Start date cannot be after end date");
        }
        if (requestModel.getStatus() == null) {
            throw new InvalidProjectDataException("Project status cannot be null");
        }
    }

    private void validateProjectRequestForUpdate(ProjectRequestModel requestModel) {
        if (requestModel.getProjectName() != null && requestModel.getProjectName().trim().isEmpty()) {
            throw new InvalidProjectDataException("Project name cannot be empty");
        }
        if (requestModel.getStartDate() != null && requestModel.getEndDate() != null
                && requestModel.getStartDate().isAfter(requestModel.getEndDate())) {
            throw new InvalidProjectDataException("Start date cannot be after end date");
        }
    }

    /**
     * Validates that all lots exist in the database.
     * 
     * @param lotIdentifiers the list of lot identifiers to validate
     * @throws NotFoundException if any lot does not exist
     */
    private void validateLotsExist(List<String> lotIdentifiers) {
        if (lotIdentifiers == null || lotIdentifiers.isEmpty()) {
            return;
        }
        
        for (String lotIdentifier : lotIdentifiers) {
            if (lotIdentifier == null || lotIdentifier.trim().isEmpty()) {
                throw new InvalidProjectDataException("Lot identifier cannot be null or empty");
            }
            if (lotRepository.findByLotIdentifier_LotId(lotIdentifier) == null) {
                throw new NotFoundException("Lot not found with identifier: " + lotIdentifier);
            }
        }
    }

    /**
     * Checks if an image identifier is valid (not null, not empty, and not the string "null").
     * 
     * @param imageIdentifier the image identifier to check
     * @return true if the identifier is valid and should be validated
     */
    private boolean isValidImageIdentifier(String imageIdentifier) {
        if (imageIdentifier == null) {
            return false;
        }
        String trimmed = imageIdentifier.trim();
        return !trimmed.isEmpty() && !trimmed.equalsIgnoreCase("null");
    }

    /**
     * Validates that an image exists in the files service asynchronously.
     * This method blocks until the validation completes (with timeout).
     * 
     * @param imageIdentifier the image identifier to validate
     * @throws InvalidProjectDataException if the image does not exist
     */
    private void validateImageExists(String imageIdentifier) {
        Boolean exists = fileServiceClient.validateFileExists(imageIdentifier)
                .timeout(Duration.ofSeconds(5))
                .blockOptional()
                .orElse(false);
        
        if (!exists) {
            throw new InvalidProjectDataException("Image not found in files service with identifier: " + imageIdentifier);
        }
    }
}