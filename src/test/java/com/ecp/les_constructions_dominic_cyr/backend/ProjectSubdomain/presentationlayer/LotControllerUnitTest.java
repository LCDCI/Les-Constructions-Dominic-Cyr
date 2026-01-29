package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
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

@WebMvcTest(LotController.class)
class LotControllerUnitTest {

    @Autowired
    private LotController lotController;

    @MockitoBean
    private LotService lotService;

    private final String FOUND_ID = UUID.randomUUID().toString();
    private final String NOT_FOUND_ID = UUID.randomUUID().toString();
    private final String INVALID_ID = "short-id";
    private final String PROJECT_ID = "proj-001-test";

    @Test
    void whenNoLotsExist_thenReturnEmptyList() {
        when(lotService.getAllLotsByProject(PROJECT_ID)).thenReturn(List.of());

        ResponseEntity<List<LotResponseModel>> resp = lotController.getAllLotsByProject(PROJECT_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(lotService, times(1)).getAllLotsByProject(PROJECT_ID);
    }

    // ==== GET ONE ====
    @Test
    void whenValidId_thenReturnLot() {
        LotResponseModel dummy = new LotResponseModel();
        dummy.setLotId(FOUND_ID);

        when(lotService.getLotById(FOUND_ID)).thenReturn(dummy);

        ResponseEntity<LotResponseModel> resp = lotController.getLotById(FOUND_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(lotService, times(1)).getLotById(FOUND_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        when(lotService.getLotById(NOT_FOUND_ID)).thenThrow(new NotFoundException("Unknown"));

        assertThrows(NotFoundException.class, () -> lotController.getLotById(NOT_FOUND_ID));
    }

    @Test
    void whenGetByIdInvalid_thenThrowInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> lotController.getLotById(INVALID_ID));
        verify(lotService, never()).getLotById(any());
    }

    // ==== POST ====
    @Test
    void whenValidCreate_thenReturnCreated() {
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-TEST-001");
        req.setCivicAddress("LocA");
        req.setPrice(100f);
        req.setDimensionsSquareFeet("1000");
        req.setDimensionsSquareMeters("92.9");

        LotResponseModel created = new LotResponseModel();
        created.setLotId(FOUND_ID);

        when(lotService.addLotToProject(PROJECT_ID, req)).thenReturn(created);

        ResponseEntity<LotResponseModel> resp = lotController.addLot(PROJECT_ID, req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertSame(created, resp.getBody());
        verify(lotService, times(1)).addLotToProject(PROJECT_ID, req);
    }

    @Test
    void whenServiceThrowsOnCreate_thenPropagate() {
        LotRequestModel req = new LotRequestModel();
        doThrow(new RuntimeException("boom")).when(lotService).addLotToProject(PROJECT_ID, req);

        assertThrows(RuntimeException.class, () -> lotController.addLot(PROJECT_ID, req));
        verify(lotService, times(1)).addLotToProject(PROJECT_ID, req);
    }

    // ==== PUT ====
    @Test
    void whenValidUpdate_thenReturnOk() {
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-TEST-002");
        req.setCivicAddress("Updated");

        LotResponseModel updated = new LotResponseModel();
        updated.setLotId(FOUND_ID);
        updated.setCivicAddress("Updated");

        when(lotService.updateLot(req, FOUND_ID)).thenReturn(updated);

        ResponseEntity<LotResponseModel> resp = lotController.updateLot(req, FOUND_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(updated, resp.getBody());
        verify(lotService, times(1)).updateLot(req, FOUND_ID);
    }

    @Test
    void whenUpdateWithInvalidId_thenThrowInvalidInputException() {
        LotRequestModel req = new LotRequestModel();
        assertThrows(InvalidInputException.class, () -> lotController.updateLot(req, INVALID_ID));
        verify(lotService, never()).updateLot(any(), anyString());
    }

    // ==== DELETE ====
    @Test
    void whenDeleteWithValidId_thenReturnNoContent() {
        // service.deleteLot is void; stub to do nothing
        doNothing().when(lotService).deleteLot(FOUND_ID);

        ResponseEntity<Void> resp = lotController.deleteLot(FOUND_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(lotService, times(1)).deleteLot(FOUND_ID);
    }

    @Test
    void whenDeleteWithInvalidId_thenThrowInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> lotController.deleteLot(INVALID_ID));
        verify(lotService, never()).deleteLot(anyString());
    }

    // ==== CUSTOMER ASSIGNMENT ====
    @Test
    void whenGetLotWithAssignedCustomer_thenReturnLotWithCustomerInfo() {
        String customerId = UUID.randomUUID().toString();
        LotResponseModel.AssignedUserInfo userInfo = LotResponseModel.AssignedUserInfo.builder()
                .userId(customerId)
                .fullName("John Customer")
                .email("john@test.com")
                .role("CUSTOMER")
                .build();

        LotResponseModel lotWithCustomer = new LotResponseModel();
        lotWithCustomer.setLotId(FOUND_ID);
        lotWithCustomer.setLotNumber("Lot-100");
        lotWithCustomer.setAssignedUsers(List.of(userInfo));

        when(lotService.getLotById(FOUND_ID)).thenReturn(lotWithCustomer);

        ResponseEntity<LotResponseModel> resp = lotController.getLotById(FOUND_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getAssignedUsers());
        assertEquals(1, resp.getBody().getAssignedUsers().size());
        assertEquals(customerId, resp.getBody().getAssignedUsers().get(0).getUserId());
        assertEquals("John Customer", resp.getBody().getAssignedUsers().get(0).getFullName());
    }

    @Test
    void whenCreateLotWithCustomer_thenReturnCreatedWithCustomer() {
        String customerId = UUID.randomUUID().toString();
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-TEST-003");
        req.setCivicAddress("CustomerLoc");
        req.setPrice(150f);
        req.setDimensionsSquareFeet("1500");
        req.setDimensionsSquareMeters("139.4");
        req.setAssignedUserIds(List.of(customerId));

        LotResponseModel.AssignedUserInfo userInfo = LotResponseModel.AssignedUserInfo.builder()
                .userId(customerId)
                .fullName("Jane Customer")
                .email("jane@test.com")
                .role("CUSTOMER")
                .build();

        LotResponseModel created = new LotResponseModel();
        created.setLotId(FOUND_ID);
        created.setAssignedUsers(List.of(userInfo));

        when(lotService.addLotToProject(PROJECT_ID, req)).thenReturn(created);

        ResponseEntity<LotResponseModel> resp = lotController.addLot(PROJECT_ID, req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getAssignedUsers());
        assertEquals(customerId, resp.getBody().getAssignedUsers().get(0).getUserId());
    }

    @Test
    void whenUpdateLotWithCustomer_thenReturnUpdatedWithCustomer() {
        String customerId = UUID.randomUUID().toString();
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-TEST-004");
        req.setCivicAddress("UpdatedCustomerLoc");
        req.setPrice(200f);
        req.setDimensionsSquareFeet("2000");
        req.setDimensionsSquareMeters("185.8");
        req.setAssignedUserIds(List.of(customerId));

        LotResponseModel.AssignedUserInfo userInfo = LotResponseModel.AssignedUserInfo.builder()
                .userId(customerId)
                .fullName("Updated Customer")
                .email("updated@test.com")
                .role("CUSTOMER")
                .build();

        LotResponseModel updated = new LotResponseModel();
        updated.setLotId(FOUND_ID);
        updated.setAssignedUsers(List.of(userInfo));

        when(lotService.updateLot(req, FOUND_ID)).thenReturn(updated);

        ResponseEntity<LotResponseModel> resp = lotController.updateLot(req, FOUND_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getAssignedUsers());
        assertEquals(customerId, resp.getBody().getAssignedUsers().get(0).getUserId());
    }
}
