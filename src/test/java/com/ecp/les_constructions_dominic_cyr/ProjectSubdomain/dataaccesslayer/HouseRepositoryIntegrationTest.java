package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.dataaccesslayer;


import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.House;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository integration tests for House entity.
 * Uses @DataJpaTest which configures an in-memory H2 DB for tests by default.
 */
@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class HouseRepositoryIntegrationTest {

    @Autowired
    private HouseRepository houseRepository;

    @Test
    @DisplayName("whenHousesExist_thenReturnAll")
    public void whenHousesExist_thenReturnAll() {
        // arrange
        House h1 = new House();
        h1.setHouseIdentifier(new HouseIdentifier("id-1"));
        h1.setHouseName("House 1");
        h1.setLocation("Loc 1");
        h1.setDescription("Desc 1");
        h1.setNumberOfRooms(5);
        h1.setNumberOfBedrooms(3);
        h1.setNumberOfBathrooms(2);
        h1.setConstructionYear(2020);

        House h2 = new House();
        h2.setHouseIdentifier(new HouseIdentifier("id-2"));
        h2.setHouseName("House 2");
        h2.setLocation("Loc 2");
        h2.setDescription("Desc 2");
        h2.setNumberOfRooms(6);
        h2.setNumberOfBedrooms(4);
        h2.setNumberOfBathrooms(3);
        h2.setConstructionYear(2021);

        houseRepository.save(h1);
        houseRepository.save(h2);
        long count = houseRepository.count();

        // act
        List<House> list = houseRepository.findAll();

        // assert
        assertNotNull(list);
        assertNotEquals(0, count);
        assertEquals(count, list.size());
    }

    @Test
    @DisplayName("whenFindByHouseId_thenReturnEntity")
    public void whenFindByHouseId_thenReturnEntity() {
        House toSave = new House();
        toSave.setHouseIdentifier(new HouseIdentifier("test-house-1"));
        toSave.setHouseName("Test House");
        toSave.setLocation("Test Loc");
        toSave.setDescription("Test Desc");
        toSave.setNumberOfRooms(4);
        toSave.setNumberOfBedrooms(2);
        toSave.setNumberOfBathrooms(1);
        toSave.setConstructionYear(2019);
        houseRepository.save(toSave);

        House found = houseRepository.findHouseByHouseIdentifier_HouseId("test-house-1");

        assertNotNull(found);
        assertEquals("test-house-1", found.getHouseIdentifier().getHouseId());
    }

    @Test
    @DisplayName("whenFindByUnknownHouseId_thenReturnNull")
    public void whenFindByUnknownHouseId_thenReturnNull() {
        House found = houseRepository.findHouseByHouseIdentifier_HouseId("no-such-house");
        assertNull(found);
    }

    @Test
    @DisplayName("whenValidEntityIsSaved_thenPersist")
    public void whenValidEntityIsSaved_thenPersist() {
        House entity = new House();
        entity.setHouseIdentifier(new HouseIdentifier("save-1"));
        entity.setHouseName("Saved House");
        entity.setLocation("Saved Loc");
        entity.setDescription("Saved Desc");
        entity.setNumberOfRooms(3);
        entity.setNumberOfBedrooms(2);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2018);

        House saved = houseRepository.save(entity);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Saved House", saved.getHouseName());
        assertEquals("save-1", saved.getHouseIdentifier().getHouseId());
    }

    @Test
    @DisplayName("whenEntityUpdated_thenChangesPersist")
    public void whenEntityUpdated_thenChangesPersist() {
        House entity = new House();
        entity.setHouseIdentifier(new HouseIdentifier("upd-1"));
        entity.setHouseName("Old House");
        entity.setLocation("Old Loc");
        entity.setDescription("Old Desc");
        entity.setNumberOfRooms(2);
        entity.setNumberOfBedrooms(1);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2015);

        House saved = houseRepository.save(entity);

        // update fields
        saved.setHouseName("New House");
        saved.setLocation("New Loc");
        saved.setDescription("New Desc");
        saved.setNumberOfRooms(4);
        House updated = houseRepository.save(saved);

        assertEquals("New House", updated.getHouseName());
        assertEquals("New Loc", updated.getLocation());
        assertEquals("New Desc", updated.getDescription());
        assertEquals(4, updated.getNumberOfRooms());
    }

    @Test
    @DisplayName("whenInsertNonExistent_thenInsertNewRecord")
    public void whenInsertNonExistent_thenInsertNewRecord() {
        House house = new House();
        house.setHouseIdentifier(new HouseIdentifier("new-1"));
        house.setHouseName("New House");
        house.setLocation("New Loc");
        house.setDescription("New Desc");
        house.setNumberOfRooms(5);
        house.setNumberOfBedrooms(3);
        house.setNumberOfBathrooms(2);
        house.setConstructionYear(2022);

        long before = houseRepository.count();
        House inserted = houseRepository.save(house);

        assertNotNull(inserted);
        assertNotNull(inserted.getId());
        assertEquals(before + 1, houseRepository.count());
    }

    @Test
    @DisplayName("whenDeleteEntity_thenReturnNullOnFind")
    public void whenDeleteEntity_thenReturnNullOnFind() {
        House entity = new House();
        entity.setHouseIdentifier(new HouseIdentifier("del-1"));
        entity.setHouseName("ToDelete");
        entity.setLocation("Del Loc");
        entity.setDescription("Del Desc");
        entity.setNumberOfRooms(3);
        entity.setNumberOfBedrooms(2);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2017);

        houseRepository.save(entity);

        House toDelete = houseRepository.findHouseByHouseIdentifier_HouseId("del-1");
        houseRepository.delete(toDelete);

        House found = houseRepository.findHouseByHouseIdentifier_HouseId("del-1");
        assertNull(found);
    }

    @Test
    @DisplayName("whenDeleteNonExistent_thenNoExceptionThrown")
    public void whenDeleteNonExistent_thenNoExceptionThrown() {
        House ghost = new House();
        ghost.setHouseIdentifier(new HouseIdentifier("ghost-del"));
        ghost.setHouseName("Ghost");
        ghost.setLocation("Ghost Loc");
        ghost.setDescription("Ghost Desc");
        ghost.setNumberOfRooms(1);
        ghost.setNumberOfBedrooms(1);
        ghost.setNumberOfBathrooms(1);
        ghost.setConstructionYear(2010);

        assertDoesNotThrow(() -> houseRepository.delete(ghost));
    }

    @Test
    @DisplayName("whenExistsByHouseName_thenReturnTrueFalse")
    public void whenExistsByHouseName_thenReturnTrueFalse() {
        House e = new House();
        e.setHouseIdentifier(new HouseIdentifier("ex-1"));
        e.setHouseName("UniqueHouseName");
        e.setLocation("Unique Loc");
        e.setDescription("Unique Desc");
        e.setNumberOfRooms(4);
        e.setNumberOfBedrooms(2);
        e.setNumberOfBathrooms(2);
        e.setConstructionYear(2023);
        houseRepository.save(e);

        List<House> all = houseRepository.findAll();
        assertTrue(all.stream().anyMatch(h -> "UniqueHouseName".equals(h.getHouseName())));
    }
}