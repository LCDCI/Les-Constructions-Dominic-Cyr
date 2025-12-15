package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectManagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectManagementPageContentRepository extends JpaRepository<ProjectManagementPageContent, Long> {
    Optional<ProjectManagementPageContent> findByLanguage(String language);
}

