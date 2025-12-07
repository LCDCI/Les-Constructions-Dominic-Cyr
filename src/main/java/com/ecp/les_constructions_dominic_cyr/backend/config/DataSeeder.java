package com.ecp.les_constructions_dominic_cyr.backend.config;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
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

    @Autowired
    private HouseRepository houseRepository;

    private static final Map<String, String> PROJECT_IMAGES = Map.of(
            "proj-001-foresta", "dcada4a5-aa19-4346-934e-1e57bc0f9e1f",
            "proj-002-naturest", "ee576ed6-5d56-4d54-ba25-7157f7b75d0d",
            "proj-003-otryminc", "4215eb96-af4b-492b-870a-6925e78b7fcc"
    );

    @PostConstruct
    public void init() {
        log.info("Running data seeder...");
        seedProjectImages();
        seedHouseImages();
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

    private static final Map<String, String> HOUSE_IMAGES = Map.of(
            "a3f1c0f1-8f2b-4c3d-9d5a-1b2a3c4d5e6f", "c15dc19e-bab3-478c-b0f7-4696acccb69d",
            "b7d2e1a4-2c6f-4b8e-9f3c-7a9b0c1d2e3f", "b4cbbd58-722a-4117-89bd-57c0bbb94970",
            "c9e3f2b5-3d7a-4f9b-8e4d-0a1b2c3d4e5f", "9fbe99e4-4ecd-423b-9414-4bc828c09ba5",
            "d1a4b3c6-4e8b-5a0c-9f1d-2c3b4a5d6e7f", "91be87e7-2bff-42d5-80b8-a91df65870ea",
            "e2b5c4d7-5f9c-6b1d-0a2e-3d4c5b6a7f8e", "46a02f41-168c-4617-8a73-985c176ad5ae",
            "f3c6d5e8-6a0d-7c2e-1b3f-4e5d6c7b8a9f", "45908df2-7c70-4cfb-a2cb-4e272551e0d6",
            "04d7e6f9-7b1e-8d3f-2c4a-5f6e7d8c9b0a", "42c6ab45-8f63-4c30-bea6-de694afb4e83",
            "158e07a1-8c2f-9e4d-3b5a-6c7d8e9f0a1b", "3676b48b-d402-4806-b1a5-77e326ae73c3",
            "269f18b2-9d3a-0f5e-4c6b-7d8e9f0a1b2c", "34c1af2c-31e4-4482-b683-c194c1da4586",
            "37a029c3-0e4b-1f6d-5d7c-8e9f0a1b2c3d", "dbb884ce-8b9e-4d40-89ea-f85af12af0b8"
    );

    private void seedHouseImages() {
        HOUSE_IMAGES.forEach((houseId, imageId) -> {
            houseRepository.findHouseByHouseIdentifier_HouseId(houseId).ifPresent(house -> {
                if (house.getImageIdentifier() == null || house.getImageIdentifier().isEmpty()) {
                    house.setImageIdentifier(imageId);
                    projectRepository.save(house);
                    log.info("Linked image to project: {}", houseId);
                }
            });
        });
    }
}
