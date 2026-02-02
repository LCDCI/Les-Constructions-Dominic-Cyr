package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest

//These lines of code must be added to each test class wishing to use a database, or otherwise all tests will fail
//This occurs because we are no longuer using h2, but rather postgresql via testcontainers and syntax in postgresql differs from h2
//This prevents syntax errors during tests
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class LotRepositoryIntegrationTest {

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    public void setUp() {
        // Create and save a test project for all lot tests
        testProject = new Project();
        testProject.setProjectIdentifier("test-project-repo");
        testProject.setProjectName("Test Project for Lots");
        testProject.setStatus(ProjectStatus.PLANNED);
        testProject.setStartDate(LocalDate.now());
        testProject.setPrimaryColor("#000000");
        testProject.setTertiaryColor("#FFFFFF");
        testProject.setBuyerColor("#CCCCCC");
        testProject.setImageIdentifier("test-image-id");
        testProject = projectRepository.save(testProject);
    }

    @Test
    @DisplayName("whenLotsExist_thenReturnAll")
    public void whenLotsExist_thenReturnAll() {
        Lot a1 = new Lot(new LotIdentifier(UUID.randomUUID().toString()), "Lot-A1", "L1", 100f, "1000", "92.9", LotStatus.AVAILABLE);
        a1.setProject(testProject);
        Lot a2 = new Lot(new LotIdentifier(UUID.randomUUID().toString()), "Lot-A2", "L2", 200f, "2000", "185.8", LotStatus.SOLD);
        a2.setProject(testProject);

        lotRepository.save(a1);
        lotRepository.save(a2);
        long count = lotRepository.count();

        List<Lot> list = lotRepository.findAll();

        assertNotNull(list);
        assertNotEquals(0, count);
        assertEquals(count, list.size());
    }

    @Test
    @DisplayName("whenFindByLotId_thenReturnEntity")
    public void whenFindByLotId_thenReturnEntity() {
        UUID lotId = UUID.randomUUID();
        Lot toSave = new Lot(new LotIdentifier(lotId.toString()), "Lot-TEST-1", "LocX", 123f, "1230", "114.3", LotStatus.AVAILABLE);
        toSave.setProject(testProject);
        lotRepository.save(toSave);

        Lot found = lotRepository.findByLotIdentifier_LotId(lotId);

        assertNotNull(found);
        assertEquals(lotId, found.getLotIdentifier().getLotId());
    }

    @Test
    @DisplayName("whenFindByUnknownLotId_thenReturnNull")
    public void whenFindByUnknownLotId_thenReturnNull() {
        Lot found = lotRepository.findByLotIdentifier_LotId(UUID.randomUUID());
        assertNull(found);
    }

    @Test
    @DisplayName("whenValidEntityIsSaved_thenPersist")
    public void whenValidEntityIsSaved_thenPersist() {
        UUID lotId = UUID.randomUUID();
        Lot entity = new Lot(new LotIdentifier(lotId.toString()), "Lot-SAVE-1", "Saved", 321f, "3210", "298.3", LotStatus.AVAILABLE);
        entity.setProject(testProject);

        Lot saved = lotRepository.save(entity);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Saved", saved.getCivicAddress());
        assertEquals(lotId, saved.getLotIdentifier().getLotId());
    }

    @Test
    @DisplayName("whenEntityUpdated_thenChangesPersist")
    public void whenEntityUpdated_thenChangesPersist() {
        Lot entity = new Lot(new LotIdentifier("upd-1"), "Lot-UPD-1", "Old", 10f, "100", "9.3", LotStatus.AVAILABLE);
        entity.setProject(testProject);

        Lot saved = lotRepository.save(entity);

        saved.setCivicAddress("New");
        saved.setDimensionsSquareFeet("200");
        saved.setDimensionsSquareMeters("18.6");
        Lot updated = lotRepository.save(saved);

        assertEquals("New", updated.getCivicAddress());
        assertEquals("200", updated.getDimensionsSquareFeet());
        assertEquals("18.6", updated.getDimensionsSquareMeters());
    }

    @Test
    @DisplayName("whenInsertNonExistent_thenInsertNewRecord")
    public void whenInsertNonExistent_thenInsertNewRecord() {
        Lot ghost = new Lot(new LotIdentifier(UUID.randomUUID().toString()), "Lot-GHOST-1", "Ghost", 5f, "50", "4.6", LotStatus.AVAILABLE);
        ghost.setProject(testProject);

        long before = lotRepository.count();
        Lot inserted = lotRepository.save(ghost);

        assertNotNull(inserted);
        assertNotNull(inserted.getId());
        assertEquals(before + 1, lotRepository.count());
    }

    @Test
    @DisplayName("whenDeleteEntity_thenReturnNullOnFind")
    public void whenDeleteEntity_thenReturnNullOnFind() {
        UUID lotId = UUID.randomUUID();
        Lot entity = new Lot(new LotIdentifier(lotId.toString()), "Lot-DEL-1", "ToDelete", 11f, "110", "10.2", LotStatus.AVAILABLE);
        entity.setProject(testProject);

        lotRepository.save(entity);

        Lot toDelete = lotRepository.findByLotIdentifier_LotId(lotId);
        lotRepository.delete(toDelete);

        Lot found = lotRepository.findByLotIdentifier_LotId(lotId);
        assertNull(found);
    }

    @Test
    @DisplayName("whenDeleteNonExistent_thenNoExceptionThrown")
    public void whenDeleteNonExistent_thenNoExceptionThrown() {
        Lot ghost = new Lot(new LotIdentifier(UUID.randomUUID().toString()), "Lot-GHOST-DEL", "G", 1f, "10", "0.9", LotStatus.AVAILABLE);
        ghost.setProject(testProject);

        assertDoesNotThrow(() -> lotRepository.delete(ghost));
    }

    @Test
    @DisplayName("whenExistsByLocation_thenReturnTrueFalse")
    public void whenExistsByLocation_thenReturnTrueFalse() {
        Lot e = new Lot(new LotIdentifier(UUID.randomUUID().toString()), "Lot-EX-1", "UniqueLoc", 99f, "990", "92.0", LotStatus.AVAILABLE);
        e.setProject(testProject);
        lotRepository.save(e);

        List<Lot> all = lotRepository.findAll();
        assertTrue(all.stream().anyMatch(l -> "UniqueLoc".equals(l.getCivicAddress())));
    }
}