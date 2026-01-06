package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.House.HouseServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.House;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House.HouseResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HouseServiceUnitTest {

    @Mock
    private HouseRepository houseRepository;

    @InjectMocks
    private HouseServiceImpl houseService;

    private House buildHouseEntity(String houseId, String houseName, String location, String description,
                                   Integer numberOfRooms, Integer numberOfBedrooms, Integer numberOfBathrooms,
                                   Integer constructionYear) {
        House house = new House();
        house.setHouseIdentifier(new HouseIdentifier(houseId));
        house.setHouseName(houseName);
        house.setLocation(location);
        house.setDescription(description);
        house.setNumberOfRooms(numberOfRooms);
        house.setNumberOfBedrooms(numberOfBedrooms);
        house.setNumberOfBathrooms(numberOfBathrooms);
        house.setConstructionYear(constructionYear);
        return house;
    }

    // ==== GET ALL ====
    @Test
    public void whenHousesExist_thenReturnAllHouses() {
        // arrange
        var e1 = buildHouseEntity("id-1", "House 1", "Location 1", "Desc 1", 5, 3, 2, 2020);
        var e2 = buildHouseEntity("id-2", "House 2", "Location 2", "Desc 2", 6, 4, 3, 2021);
        when(houseRepository.findAll()).thenReturn(List.of(e1, e2));

        // act
        List<HouseResponseModel> list = houseService.getAllHouses();

        // assert
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("House 1", list.get(0).getHouseName());
        assertEquals("Location 1", list.get(0).getLocation());
        verify(houseRepository, times(1)).findAll();
    }

    @Test
    public void whenNoHousesExist_thenReturnEmptyList() {
        when(houseRepository.findAll()).thenReturn(List.of());

        List<HouseResponseModel> list = houseService.getAllHouses();

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(houseRepository, times(1)).findAll();
    }

    // ==== GET BY ID ====
    @Test
    public void whenGetByIdFound_thenReturnDto() {
        String id = "found-id-1";
        var entity = buildHouseEntity(id, "Found House", "Found Loc", "Found Desc", 4, 2, 1, 2019);
        when(houseRepository.findHouseByHouseIdentifier_HouseId(id)).thenReturn(entity);

        HouseResponseModel resp = houseService.getHouseById(id);

        assertNotNull(resp);
        assertEquals(id, resp.getHouseId());
        assertEquals("Found House", resp.getHouseName());
        assertEquals("Found Loc", resp.getLocation());
        assertEquals("Found Desc", resp.getDescription());
        assertEquals(4, resp.getNumberOfRooms());
        assertEquals(2, resp.getNumberOfBedrooms());
        assertEquals(1, resp.getNumberOfBathrooms());
        assertEquals(2019, resp.getConstructionYear());
        verify(houseRepository, times(1)).findHouseByHouseIdentifier_HouseId(id);
    }

    @Test
    public void whenGetByIdNotFound_thenThrowNotFound() {
        String id = "no-such-id";
        when(houseRepository.findHouseByHouseIdentifier_HouseId(id)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> houseService.getHouseById(id));
        assertTrue(ex.getMessage().contains("Unknown House Id"));
        verify(houseRepository, times(1)).findHouseByHouseIdentifier_HouseId(id);
    }
}