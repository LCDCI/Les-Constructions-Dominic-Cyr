package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LivingEnvironmentContentRepository extends JpaRepository<LivingEnvironmentContent, Long> {
    
    Optional<LivingEnvironmentContent> findByProjectIdentifierAndLanguage(String projectIdentifier, String language);
    
    Optional<LivingEnvironmentContent> findByProjectIdentifier(String projectIdentifier);
}
