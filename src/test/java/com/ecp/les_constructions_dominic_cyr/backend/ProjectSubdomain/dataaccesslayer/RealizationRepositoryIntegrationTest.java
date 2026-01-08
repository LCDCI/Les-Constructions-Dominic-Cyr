package  com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;


import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.Realization;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository integration tests for Realization entity.
 * Uses @DataJpaTest which configures an in-memory H2 DB for tests by default.
 */
@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class RealizationRepositoryIntegrationTest {

    @Autowired
    private RealizationRepository realizationRepository;

    @Test
    @DisplayName("whenRealizationsExist_thenReturnAll")
    public void whenRealizationsExist_thenReturnAll() {
        // arrange
        Realization r1 = new Realization();
        r1.setRealizationIdentifier(new RealizationIdentifier("id-1"));
        r1.setRealizationName("Realization 1");
        r1.setLocation("Loc 1");
        r1.setDescription("Desc 1");
        r1.setNumberOfRooms(5);
        r1.setNumberOfBedrooms(3);
        r1.setNumberOfBathrooms(2);
        r1.setConstructionYear(2020);

        Realization r2 = new Realization();
        r2.setRealizationIdentifier(new RealizationIdentifier("id-2"));
        r2.setRealizationName("Realization 2");
        r2.setLocation("Loc 2");
        r2.setDescription("Desc 2");
        r2.setNumberOfRooms(6);
        r2.setNumberOfBedrooms(4);
        r2.setNumberOfBathrooms(3);
        r2.setConstructionYear(2021);

        realizationRepository.save(r1);
        realizationRepository.save(r2);
        long count = realizationRepository.count();

        // act
        List<Realization> list = realizationRepository.findAll();

        // assert
        assertNotNull(list);
        assertNotEquals(0, count);
        assertEquals(count, list.size());
    }

    @Test
    @DisplayName("whenFindByRealizationId_thenReturnEntity")
    public void whenFindByRealizationId_thenReturnEntity() {
        Realization toSave = new Realization();
        toSave.setRealizationIdentifier(new RealizationIdentifier("test-realization-1"));
        toSave.setRealizationName("Test Realization");
        toSave.setLocation("Test Loc");
        toSave.setDescription("Test Desc");
        toSave.setNumberOfRooms(4);
        toSave.setNumberOfBedrooms(2);
        toSave.setNumberOfBathrooms(1);
        toSave.setConstructionYear(2019);
        realizationRepository.save(toSave);

        Realization found = realizationRepository.findRealizationByRealizationIdentifier_RealizationId("test-realization-1");

        assertNotNull(found);
        assertEquals("test-realization-1", found.getRealizationIdentifier().getRealizationId());
    }

    @Test
    @DisplayName("whenFindByUnknownRealizationId_thenReturnNull")
    public void whenFindByUnknownRealizationId_thenReturnNull() {
        Realization found = realizationRepository.findRealizationByRealizationIdentifier_RealizationId("no-such-realization");
        assertNull(found);
    }

    @Test
    @DisplayName("whenValidEntityIsSaved_thenPersist")
    public void whenValidEntityIsSaved_thenPersist() {
        Realization entity = new Realization();
        entity.setRealizationIdentifier(new RealizationIdentifier("save-1"));
        entity.setRealizationName("Saved Realization");
        entity.setLocation("Saved Loc");
        entity.setDescription("Saved Desc");
        entity.setNumberOfRooms(3);
        entity.setNumberOfBedrooms(2);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2018);

        Realization saved = realizationRepository.save(entity);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Saved Realization", saved.getRealizationName());
        assertEquals("save-1", saved.getRealizationIdentifier().getRealizationId());
    }

    @Test
    @DisplayName("whenEntityUpdated_thenChangesPersist")
    public void whenEntityUpdated_thenChangesPersist() {
        Realization entity = new Realization();
        entity.setRealizationIdentifier(new RealizationIdentifier("upd-1"));
        entity.setRealizationName("Old Realization");
        entity.setLocation("Old Loc");
        entity.setDescription("Old Desc");
        entity.setNumberOfRooms(2);
        entity.setNumberOfBedrooms(1);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2015);

        Realization saved = realizationRepository.save(entity);

        // update fields
        saved.setRealizationName("New Realization");
        saved.setLocation("New Loc");
        saved.setDescription("New Desc");
        saved.setNumberOfRooms(4);
        Realization updated = realizationRepository.save(saved);

        assertEquals("New Realization", updated.getRealizationName());
        assertEquals("New Loc", updated.getLocation());
        assertEquals("New Desc", updated.getDescription());
        assertEquals(4, updated.getNumberOfRooms());
    }

    @Test
    @DisplayName("whenInsertNonExistent_thenInsertNewRecord")
    public void whenInsertNonExistent_thenInsertNewRecord() {
        Realization realization = new Realization();
        realization.setRealizationIdentifier(new RealizationIdentifier("new-1"));
        realization.setRealizationName("New Realization");
        realization.setLocation("New Loc");
        realization.setDescription("New Desc");
        realization.setNumberOfRooms(5);
        realization.setNumberOfBedrooms(3);
        realization.setNumberOfBathrooms(2);
        realization.setConstructionYear(2022);

        long before = realizationRepository.count();
        Realization inserted = realizationRepository.save(realization);

        assertNotNull(inserted);
        assertNotNull(inserted.getId());
        assertEquals(before + 1, realizationRepository.count());
    }

    @Test
    @DisplayName("whenDeleteEntity_thenReturnNullOnFind")
    public void whenDeleteEntity_thenReturnNullOnFind() {
        Realization entity = new Realization();
        entity.setRealizationIdentifier(new RealizationIdentifier("del-1"));
        entity.setRealizationName("ToDelete");
        entity.setLocation("Del Loc");
        entity.setDescription("Del Desc");
        entity.setNumberOfRooms(3);
        entity.setNumberOfBedrooms(2);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2017);

        realizationRepository.save(entity);

        Realization toDelete = realizationRepository.findRealizationByRealizationIdentifier_RealizationId("del-1");
        realizationRepository.delete(toDelete);

        Realization found = realizationRepository.findRealizationByRealizationIdentifier_RealizationId("del-1");
        assertNull(found);
    }

    @Test
    @DisplayName("whenDeleteNonExistent_thenNoExceptionThrown")
    public void whenDeleteNonExistent_thenNoExceptionThrown() {
        Realization ghost = new Realization();
        ghost.setRealizationIdentifier(new RealizationIdentifier("ghost-del"));
        ghost.setRealizationName("Ghost");
        ghost.setLocation("Ghost Loc");
        ghost.setDescription("Ghost Desc");
        ghost.setNumberOfRooms(1);
        ghost.setNumberOfBedrooms(1);
        ghost.setNumberOfBathrooms(1);
        ghost.setConstructionYear(2010);

        assertDoesNotThrow(() -> realizationRepository.delete(ghost));
    }

    @Test
    @DisplayName("whenExistsByRealizationName_thenReturnTrueFalse")
    public void whenExistsByRealizationName_thenReturnTrueFalse() {
        Realization e = new Realization();
        e.setRealizationIdentifier(new RealizationIdentifier("ex-1"));
        e.setRealizationName("UniqueRealizationName");
        e.setLocation("Unique Loc");
        e.setDescription("Unique Desc");
        e.setNumberOfRooms(4);
        e.setNumberOfBedrooms(2);
        e.setNumberOfBathrooms(2);
        e.setConstructionYear(2023);
        realizationRepository.save(e);

        List<Realization> all = realizationRepository.findAll();
        assertTrue(all.stream().anyMatch(r -> "UniqueRealizationName".equals(r.getRealizationName())));
    }
}
