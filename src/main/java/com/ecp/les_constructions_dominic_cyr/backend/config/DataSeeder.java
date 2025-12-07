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
            "a12b3c45-d678-4efa-9012-bc3456de789f", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "b23c4d56-e789-5fab-0123-cd4567ef890a", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "c34d5e67-f890-6gcb-1234-de5678fg901b", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "d45e6f78-g901-7hdb-2345-ef6789gh012c", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "e56f7g89-h012-8iec-3456-fg7890hi123d", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "f67890ab-i123-9jfd-4567-gh8901ij234e", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "g78901bc-j234-0kge-5678-hi9012jk345f", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "h89012cd-k345-1lgf-6789-ij0123kl456g", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "i90123de-l456-2mhg-7890-jk1234lm567h", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c",
            "j01234ef-m567-3nih-8901-kl2345mn678i", "ea6635b6-f380-4e01-aee8-5e1dfd4e853c"
    );

    private void seedLotImages() {
        HOUSE_IMAGES.forEach((lotId, imageId) -> {
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
