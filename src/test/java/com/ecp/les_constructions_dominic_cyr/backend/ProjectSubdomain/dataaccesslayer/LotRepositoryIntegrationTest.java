package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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

    @Test
    @DisplayName("whenLotsExist_thenReturnAll")
    public void whenLotsExist_thenReturnAll() {
        Lot a1 = new Lot();
        a1.setLotIdentifier(new LotIdentifier("id-1"));
        a1.setLocation("L1");
        a1.setPrice(100f);
        a1.setDimensions("10x10");
        a1.setLotStatus(LotStatus.AVAILABLE);

        Lot a2 = new Lot();
        a2.setLotIdentifier(new LotIdentifier("id-2"));
        a2.setLocation("L2");
        a2.setPrice(200f);
        a2.setDimensions("20x20");
        a2.setLotStatus(LotStatus.SOLD);

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
        Lot toSave = new Lot();
        toSave.setLotIdentifier(new LotIdentifier("test-lot-1"));
        toSave.setLocation("LocX");
        toSave.setPrice(123f);
        toSave.setDimensions("12x12");
        toSave.setLotStatus(LotStatus.AVAILABLE);
        lotRepository.save(toSave);

        Lot found = lotRepository.findByLotIdentifier_LotId("test-lot-1");

        assertNotNull(found);
        assertEquals("test-lot-1", found.getLotIdentifier().getLotId());
    }

    @Test
    @DisplayName("whenFindByUnknownLotId_thenReturnNull")
    public void whenFindByUnknownLotId_thenReturnNull() {
        Lot found = lotRepository.findByLotIdentifier_LotId("no-such-lot");
        assertNull(found);
    }

    @Test
    @DisplayName("whenValidEntityIsSaved_thenPersist")
    public void whenValidEntityIsSaved_thenPersist() {
        Lot entity = new Lot();
        entity.setLotIdentifier(new LotIdentifier("save-1"));
        entity.setLocation("Saved");
        entity.setPrice(321f);
        entity.setDimensions("3x3");
        entity.setLotStatus(LotStatus.AVAILABLE);

        Lot saved = lotRepository.save(entity);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Saved", saved.getLocation());
        assertEquals("save-1", saved.getLotIdentifier().getLotId());
    }

    @Test
    @DisplayName("whenEntityUpdated_thenChangesPersist")
    public void whenEntityUpdated_thenChangesPersist() {
        Lot entity = new Lot();
        entity.setLotIdentifier(new LotIdentifier("upd-1"));
        entity.setLocation("Old");
        entity.setPrice(10f);
        entity.setDimensions("1x1");
        entity.setLotStatus(LotStatus.AVAILABLE);

        Lot saved = lotRepository.save(entity);

        saved.setLocation("New");
        saved.setDimensions("2x2");
        Lot updated = lotRepository.save(saved);

        assertEquals("New", updated.getLocation());
        assertEquals("2x2", updated.getDimensions());
    }

    @Test
    @DisplayName("whenInsertNonExistent_thenInsertNewRecord")
    public void whenInsertNonExistent_thenInsertNewRecord() {
        Lot ghost = new Lot();
        ghost.setLotIdentifier(new LotIdentifier("ghost-1"));
        ghost.setLocation("Ghost");
        ghost.setPrice(5f);
        ghost.setDimensions("0x0");
        ghost.setLotStatus(LotStatus.AVAILABLE);

        long before = lotRepository.count();
        Lot inserted = lotRepository.save(ghost);

        assertNotNull(inserted);
        assertNotNull(inserted.getId());
        assertEquals(before + 1, lotRepository.count());
    }

    @Test
    @DisplayName("whenDeleteEntity_thenReturnNullOnFind")
    public void whenDeleteEntity_thenReturnNullOnFind() {
        Lot entity = new Lot();
        entity.setLotIdentifier(new LotIdentifier("del-1"));
        entity.setLocation("ToDelete");
        entity.setPrice(11f);
        entity.setDimensions("11x11");
        entity.setLotStatus(LotStatus.AVAILABLE);

        lotRepository.save(entity);

        Lot toDelete = lotRepository.findByLotIdentifier_LotId("del-1");
        lotRepository.delete(toDelete);

        Lot found = lotRepository.findByLotIdentifier_LotId("del-1");
        assertNull(found);
    }

    @Test
    @DisplayName("whenDeleteNonExistent_thenNoExceptionThrown")
    public void whenDeleteNonExistent_thenNoExceptionThrown() {
        Lot ghost = new Lot();
        ghost.setLotIdentifier(new LotIdentifier("ghost-del"));
        ghost.setLocation("G");
        ghost.setPrice(1f);
        ghost.setDimensions("1x1");
        ghost.setLotStatus(LotStatus.AVAILABLE);

        assertDoesNotThrow(() -> lotRepository.delete(ghost));
    }

    @Test
    @DisplayName("whenExistsByLocation_thenReturnTrueFalse")
    public void whenExistsByLocation_thenReturnTrueFalse() {
        Lot e = new Lot();
        e.setLotIdentifier(new LotIdentifier("ex-1"));
        e.setLocation("UniqueLoc");
        e.setPrice(99f);
        e.setDimensions("9x9");
        e.setLotStatus(LotStatus.AVAILABLE);
        lotRepository.save(e);

        List<Lot> all = lotRepository.findAll();
        assertTrue(all.stream().anyMatch(l -> "UniqueLoc".equals(l.getLocation())));
    }
}