package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Realization.RealizationService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization.RealizationController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization.RealizationResponseModel;
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

@WebMvcTest(RealizationController.class)
class RealizationControllerUnitTest {

    @Autowired
    private RealizationController realizationController;

    @MockitoBean
    private RealizationService realizationService;

    private final String FOUND_ID = UUID.randomUUID().toString();
    private final String NOT_FOUND_ID = UUID.randomUUID().toString();
    private final String INVALID_ID = "short-id";

    @Test
    void whenNoRealizationsExist_thenReturnEmptyList() {
        when(realizationService.getAllRealizations()).thenReturn(List.of());

        ResponseEntity<List<RealizationResponseModel>> resp = realizationController.getAllRealizations();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(realizationService, times(1)).getAllRealizations();
    }

    @Test
    void whenRealizationsExist_thenReturnList() {
        RealizationResponseModel realization1 = new RealizationResponseModel();
        realization1.setRealizationId(FOUND_ID);
        realization1.setRealizationName("Realization 1");

        RealizationResponseModel realization2 = new RealizationResponseModel();
        realization2.setRealizationId(UUID.randomUUID().toString());
        realization2.setRealizationName("Realization 2");

        when(realizationService.getAllRealizations()).thenReturn(List.of(realization1, realization2));

        ResponseEntity<List<RealizationResponseModel>> resp = realizationController.getAllRealizations();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        verify(realizationService, times(1)).getAllRealizations();
    }

    // ==== GET ONE ====
    @Test
    void whenValidId_thenReturnRealization() {
        RealizationResponseModel dummy = new RealizationResponseModel();
        dummy.setRealizationId(FOUND_ID);
        dummy.setRealizationName("Test Realization");

        when(realizationService.getRealizationById(FOUND_ID)).thenReturn(dummy);

        ResponseEntity<RealizationResponseModel> resp = realizationController.getRealizationById(FOUND_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(realizationService, times(1)).getRealizationById(FOUND_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        when(realizationService.getRealizationById(NOT_FOUND_ID)).thenThrow(new NotFoundException("Unknown"));

        assertThrows(NotFoundException.class, () -> realizationController.getRealizationById(NOT_FOUND_ID));
    }

    @Test
    void whenGetByIdInvalid_thenThrowInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> realizationController.getRealizationById(INVALID_ID));
        verify(realizationService, never()).getRealizationById(any());
    }

    @Test
    void whenGetByIdInvalidUUIDFormat_thenThrowInvalidInputException() {
        // Valid length (36) but invalid UUID format
        String invalidUUID = "12345678-1234-1234-1234-12345678901X";
        assertThrows(InvalidInputException.class, () -> realizationController.getRealizationById(invalidUUID));
        verify(realizationService, never()).getRealizationById(any());
    }
}
