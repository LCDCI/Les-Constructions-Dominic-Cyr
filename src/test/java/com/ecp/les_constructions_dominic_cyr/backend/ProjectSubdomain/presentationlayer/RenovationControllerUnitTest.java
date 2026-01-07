package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Renovation.RenovationService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationResponseModel;
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

@WebMvcTest(RenovationController.class)
class RenovationControllerUnitTest {

    @Autowired
    private RenovationController renovationController;

    @MockitoBean
    private RenovationService renovationService;

    private final String FOUND_ID = UUID.randomUUID().toString();
    private final String NOT_FOUND_ID = UUID.randomUUID().toString();
    private final String INVALID_ID = "short-id";

    @Test
    void whenNoRenovationsExist_thenReturnEmptyList() {
        when(renovationService.getAllRenovations()).thenReturn(List.of());

        ResponseEntity<List<RenovationResponseModel>> resp = renovationController.getAllRenovations();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(renovationService, times(1)).getAllRenovations();
    }

    @Test
    void whenRenovationsExist_thenReturnList() {
        RenovationResponseModel renovation1 = new RenovationResponseModel();
        renovation1.setRenovationId(FOUND_ID);
        renovation1.setDescription("Renovation 1");

        RenovationResponseModel renovation2 = new RenovationResponseModel();
        renovation2.setRenovationId(UUID.randomUUID().toString());
        renovation2.setDescription("Renovation 2");

        when(renovationService.getAllRenovations()).thenReturn(List.of(renovation1, renovation2));

        ResponseEntity<List<RenovationResponseModel>> resp = renovationController.getAllRenovations();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        verify(renovationService, times(1)).getAllRenovations();
    }

    // ==== GET ONE ====
    @Test
    void whenValidId_thenReturnRenovation() {
        RenovationResponseModel dummy = new RenovationResponseModel();
        dummy.setRenovationId(FOUND_ID);
        dummy.setDescription("Test Renovation");

        when(renovationService.getRenovationById(FOUND_ID)).thenReturn(dummy);

        ResponseEntity<RenovationResponseModel> resp = renovationController.getRenovationById(FOUND_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(renovationService, times(1)).getRenovationById(FOUND_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        when(renovationService.getRenovationById(NOT_FOUND_ID)).thenThrow(new NotFoundException("Unknown"));

        assertThrows(NotFoundException.class, () -> renovationController.getRenovationById(NOT_FOUND_ID));
    }

    @Test
    void whenGetByIdInvalid_thenThrowInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> renovationController.getRenovationById(INVALID_ID));
        verify(renovationService, never()).getRenovationById(any());
    }

    @Test
    void whenGetByIdInvalidUUIDFormat_thenThrowInvalidInputException() {
        // Valid length (36) but invalid UUID format
        String invalidUUID = "12345678-1234-1234-1234-12345678901X";
        assertThrows(InvalidInputException.class, () -> renovationController.getRenovationById(invalidUUID));
        verify(renovationService, never()).getRenovationById(any());
    }
}
