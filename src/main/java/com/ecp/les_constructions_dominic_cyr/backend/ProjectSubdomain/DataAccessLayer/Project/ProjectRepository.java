package com.ecp. les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project;

import org.springframework.data. jpa.repository.JpaRepository;
import org.springframework.data. jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDate;
import java. util.List;
import java. util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Optional<Project> findByProjectIdentifier(String projectIdentifier);
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByCustomerId(String customerId);
    List<Project> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<Project> findByEndDateBetween(LocalDate startDate, LocalDate endDate);
}