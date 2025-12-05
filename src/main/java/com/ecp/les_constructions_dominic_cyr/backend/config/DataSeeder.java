package com.ecp.les_constructions_dominic_cyr.backend.config;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private ProjectRepository projectRepository;

    private static final Map<String, String> PROJECT_IMAGES = Map.of(
            "proj-001-foresta", "dcada4a5-aa19-4346-934e-1e57bc0f9e1f",
            "proj-002-naturest", "ee576ed6-5d56-4d54-ba25-7157f7b75d0d",
            "proj-003-otryminc", "4215eb96-af4b-492b-870a-6925e78b7fcc"
    );

    @PostConstruct
    public void init() {
        log.info("Running data seeder...");
        seedProjectImages();
    }

    private void seedProjectImages() {
        PROJECT_IMAGES.forEach((projectId, imageId) -> {
            projectRepository.findByProjectIdentifier(projectId).ifPresent(project -> {
                if (project.getImageIdentifier() == null || project.getImageIdentifier().isEmpty()) {
                    project.setImageIdentifier(imageId);
                    projectRepository.save(project);
                    log.info("Linked image to project: {}", projectId);
                }
            });
        });
    }
}
