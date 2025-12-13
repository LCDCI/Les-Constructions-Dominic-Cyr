package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.ProjectOverview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectGalleryImageRepository extends JpaRepository<ProjectGalleryImage, Long> {
    List<ProjectGalleryImage> findByProjectIdentifierOrderByDisplayOrderAsc(String projectIdentifier);
}