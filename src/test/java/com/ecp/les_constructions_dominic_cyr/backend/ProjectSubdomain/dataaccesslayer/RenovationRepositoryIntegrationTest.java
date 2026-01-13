package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;
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
public class RenovationRepositoryIntegrationTest {

    @Autowired
    private RenovationRepository renovationRepository;

    @Test
    @DisplayName("whenRenovationsExist_thenReturnAll")
    public void whenRenovationsExist_thenReturnAll() {
        Renovation r1 = new Renovation();
        r1.setRenovationIdentifier(new RenovationIdentifier("id-1"));
        r1.setBeforeImageIdentifier("before-1");
        r1.setAfterImageIdentifier("after-1");
        r1.setDescription("Desc 1");

        Renovation r2 = new Renovation();
        r2.setRenovationIdentifier(new RenovationIdentifier("id-2"));
        r2.setBeforeImageIdentifier("before-2");
        r2.setAfterImageIdentifier("after-2");
        r2.setDescription("Desc 2");

        renovationRepository.save(r1);
        renovationRepository.save(r2);
        long count = renovationRepository.count();

        List<Renovation> list = renovationRepository.findAll();

        assertNotNull(list);
        assertNotEquals(0, count);
        assertEquals(count, list.size());
    }

    @Test
    @DisplayName("whenFindByRenovationId_thenReturnEntity")
    public void whenFindByRenovationId_thenReturnEntity() {
        Renovation toSave = new Renovation();
        toSave.setRenovationIdentifier(new RenovationIdentifier("test-renovation-1"));
        toSave.setBeforeImageIdentifier("before-test");
        toSave.setAfterImageIdentifier("after-test");
        toSave.setDescription("Test Desc");
        renovationRepository.save(toSave);

        Renovation found = renovationRepository.findRenovationByRenovationIdentifier_RenovationId("test-renovation-1");

        assertNotNull(found);
        assertEquals("test-renovation-1", found.getRenovationIdentifier().getRenovationId());
    }

    @Test
    @DisplayName("whenFindByUnknownRenovationId_thenReturnNull")
    public void whenFindByUnknownRenovationId_thenReturnNull() {
        Renovation found = renovationRepository.findRenovationByRenovationIdentifier_RenovationId("no-such-renovation");
        assertNull(found);
    }

    @Test
    @DisplayName("whenValidEntityIsSaved_thenPersist")
    public void whenValidEntityIsSaved_thenPersist() {
        Renovation entity = new Renovation();
        entity.setRenovationIdentifier(new RenovationIdentifier("save-1"));
        entity.setBeforeImageIdentifier("before-save");
        entity.setAfterImageIdentifier("after-save");
        entity.setDescription("Saved Desc");

        Renovation saved = renovationRepository.save(entity);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Saved Desc", saved.getDescription());
        assertEquals("save-1", saved.getRenovationIdentifier().getRenovationId());
    }

    @Test
    @DisplayName("whenEntityUpdated_thenChangesPersist")
    public void whenEntityUpdated_thenChangesPersist() {
        Renovation entity = new Renovation();
        entity.setRenovationIdentifier(new RenovationIdentifier("upd-1"));
        entity.setBeforeImageIdentifier("before-old");
        entity.setAfterImageIdentifier("after-old");
        entity.setDescription("Old Desc");

        Renovation saved = renovationRepository.save(entity);

        saved.setDescription("New Desc");
        saved.setAfterImageIdentifier("after-new");
        Renovation updated = renovationRepository.save(saved);

        assertEquals("New Desc", updated.getDescription());
        assertEquals("after-new", updated.getAfterImageIdentifier());
    }

    @Test
    @DisplayName("whenInsertNonExistent_thenInsertNewRecord")
    public void whenInsertNonExistent_thenInsertNewRecord() {
        Renovation renovation = new Renovation();
        renovation.setRenovationIdentifier(new RenovationIdentifier("new-1"));
        renovation.setBeforeImageIdentifier("before-new");
        renovation.setAfterImageIdentifier("after-new");
        renovation.setDescription("New Desc");

        long before = renovationRepository.count();
        Renovation inserted = renovationRepository.save(renovation);

        assertNotNull(inserted);
        assertNotNull(inserted.getId());
        assertEquals(before + 1, renovationRepository.count());
    }

    @Test
    @DisplayName("whenDeleteEntity_thenReturnNullOnFind")
    public void whenDeleteEntity_thenReturnNullOnFind() {
        Renovation entity = new Renovation();
        entity.setRenovationIdentifier(new RenovationIdentifier("del-1"));
        entity.setBeforeImageIdentifier("before-del");
        entity.setAfterImageIdentifier("after-del");
        entity.setDescription("ToDelete");

        renovationRepository.save(entity);

        Renovation toDelete = renovationRepository.findRenovationByRenovationIdentifier_RenovationId("del-1");
        renovationRepository.delete(toDelete);

        Renovation found = renovationRepository.findRenovationByRenovationIdentifier_RenovationId("del-1");
        assertNull(found);
    }

    @Test
    @DisplayName("whenDeleteNonExistent_thenNoExceptionThrown")
    public void whenDeleteNonExistent_thenNoExceptionThrown() {
        Renovation ghost = new Renovation();
        ghost.setRenovationIdentifier(new RenovationIdentifier("ghost-del"));
        ghost.setBeforeImageIdentifier("before-ghost");
        ghost.setAfterImageIdentifier("after-ghost");
        ghost.setDescription("Ghost");

        assertDoesNotThrow(() -> renovationRepository.delete(ghost));
    }

    @Test
    @DisplayName("whenExistsByDescription_thenReturnTrueFalse")
    public void whenExistsByDescription_thenReturnTrueFalse() {
        Renovation e = new Renovation();
        e.setRenovationIdentifier(new RenovationIdentifier("ex-1"));
        e.setBeforeImageIdentifier("before-unique");
        e.setAfterImageIdentifier("after-unique");
        e.setDescription("UniqueDescription");
        renovationRepository.save(e);

        List<Renovation> all = renovationRepository.findAll();
        assertTrue(all.stream().anyMatch(r -> "UniqueDescription".equals(r.getDescription())));
    }
}
