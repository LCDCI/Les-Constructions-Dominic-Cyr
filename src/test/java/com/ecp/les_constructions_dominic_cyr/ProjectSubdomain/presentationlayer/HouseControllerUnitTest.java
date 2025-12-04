package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.House.HouseService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House.HouseController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.House.HouseResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(HouseController.class)
class HouseControllerUnitTest {

    @Autowired
    private HouseController houseController;

    @MockitoBean
    private HouseService houseService;

    private final String FOUND_ID = UUID.randomUUID().toString();
    private final String NOT_FOUND_ID = UUID.randomUUID().toString();
    private final String INVALID_ID = "short-id";

    @Test
    void whenNoHousesExist_thenReturnEmptyList() {
        when(houseService.getAllHouses()).thenReturn(List.of());

        ResponseEntity<List<HouseResponseModel>> resp = houseController.getAllHouses();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(houseService, times(1)).getAllHouses();
    }

    @Test
    void whenHousesExist_thenReturnList() {
        HouseResponseModel house1 = new HouseResponseModel();
        house1.setHouseId(FOUND_ID);
        house1.setHouseName("House 1");
        
        HouseResponseModel house2 = new HouseResponseModel();
        house2.setHouseId(UUID.randomUUID().toString());
        house2.setHouseName("House 2");

        when(houseService.getAllHouses()).thenReturn(List.of(house1, house2));

        ResponseEntity<List<HouseResponseModel>> resp = houseController.getAllHouses();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        verify(houseService, times(1)).getAllHouses();
    }

    // ==== GET ONE ====
    @Test
    void whenValidId_thenReturnHouse() {
        HouseResponseModel dummy = new HouseResponseModel();
        dummy.setHouseId(FOUND_ID);
        dummy.setHouseName("Test House");

        when(houseService.getHouseById(FOUND_ID)).thenReturn(dummy);

        ResponseEntity<HouseResponseModel> resp = houseController.getHouseById(FOUND_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(houseService, times(1)).getHouseById(FOUND_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        when(houseService.getHouseById(NOT_FOUND_ID)).thenThrow(new NotFoundException("Unknown"));

        assertThrows(NotFoundException.class, () -> houseController.getHouseById(NOT_FOUND_ID));
    }

    @Test
    void whenGetByIdInvalid_thenThrowInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> houseController.getHouseById(INVALID_ID));
        verify(houseService, never()).getHouseById(any());
    }

    @Test
    void whenGetByIdInvalidUUIDFormat_thenThrowInvalidInputException() {
        // Valid length (36) but invalid UUID format
        String invalidUUID = "12345678-1234-1234-1234-12345678901X";
        assertThrows(InvalidInputException.class, () -> houseController.getHouseById(invalidUUID));
        verify(houseService, never()).getHouseById(any());
    }
}
