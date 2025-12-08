package com.ecp.les_constructions_dominic_cyr.backend.config;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.House;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
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

    @Autowired
    private LotRepository lotRepository;

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
        seedLotImages();
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
            House house = houseRepository.findHouseByHouseIdentifier_HouseId(houseId);
            if (house != null) {
                if (house.getImageIdentifier() == null || house.getImageIdentifier().isEmpty()) {
                    house.setImageIdentifier(imageId);
                    houseRepository.save(house);
                    log.info("Linked image to house: {}", houseId);
                }
            }
        });
    }

    private static final Map<String, String> LOTS_IMAGES = Map.of(
            "f3c8837d-bd65-4bc5-9f01-cb9082fc657e", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "5a82954c-8e2c-466a-8a8f-9983b79ede63", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "cd465054-403e-4861-b9ab-1b672672c053", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "a51e7923-7a46-4e65-8cee-8783126e780b", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "64f2d3b1-eb36-49d6-8bc3-a816d97ddeb9", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "3b9b8bf2-7ea4-4b3a-9250-53ccb1a77f87", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "02088623-dd3c-4fef-af67-2caf60dc1902", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "97fd170d-189b-4c4c-880d-31893a146712", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "db43c148-68de-4882-818a-d15dc8d5fcdb", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "adb6f5b7-e036-49cf-899e-a39dcaecd91f", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c"
    );

    private void seedLotImages() {
        LOTS_IMAGES.forEach((lotId, imageId) -> {
            Lot lot = lotRepository.findByLotIdentifier_LotId(lotId);
            if (lot != null) {
                if (lot.getImageIdentifier() == null || lot.getImageIdentifier().isEmpty()) {
                    lot.setImageIdentifier(imageId);
                    lotRepository.save(lot);
                    log.info("Linked image to lot: {}", lotId);
                }
            }
        });
    }
}
