package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectOverviewContentRepository extends JpaRepository<ProjectOverviewContent, Long> {
    Optional<ProjectOverviewContent> findByProjectIdentifier(String projectIdentifier);
}