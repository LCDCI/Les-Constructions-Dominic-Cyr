package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
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
public class LotServiceUnitTest {

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private LotServiceImpl lotService;

    private Lot buildLotEntity(String lotId, String civicAddress, float price, String dimsSqFt, String dimsSqM, LotStatus status) {
        var id = new LotIdentifier(lotId);
        return new Lot(id, civicAddress, price, dimsSqFt, dimsSqM, status);
    }

    // ==== GET ALL ====
    @Test
    public void whenLotsExist_thenReturnAllLots() {
        // arrange
        var e1 = buildLotEntity("id-1", "Loc1", 100f, "1000", "92.9", LotStatus.AVAILABLE);
        var e2 = buildLotEntity("id-2", "Loc2", 200f, "2000", "185.8", LotStatus.SOLD);
        when(lotRepository.findAll()).thenReturn(List.of(e1, e2));

        // act
        List<LotResponseModel> list = lotService.getAllLots();

        // assert
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Loc1", list.get(0).getCivicAddress());
        verify(lotRepository, times(1)).findAll();
    }

    @Test
    public void whenNoLotsExist_thenReturnEmptyList() {
        when(lotRepository.findAll()).thenReturn(List.of());

        List<LotResponseModel> list = lotService.getAllLots();

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(lotRepository, times(1)).findAll();
    }

    // ==== GET BY ID ====
    @Test
    public void whenGetByIdFound_thenReturnDto() {
        String id = "found-id-1";
        var entity = buildLotEntity(id, "FoundLoc", 150f, "1500", "139.4", LotStatus.AVAILABLE);
        when(lotRepository.findByLotIdentifier_LotId(id)).thenReturn(entity);

        LotResponseModel resp = lotService.getLotById(id);

        assertNotNull(resp);
        assertEquals(id, resp.getLotId());
        assertEquals("FoundLoc", resp.getCivicAddress());
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(id);
    }

    @Test
    public void whenGetByIdNotFound_thenThrowNotFound() {
        String id = "no-such-id";
        when(lotRepository.findByLotIdentifier_LotId(id)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> lotService.getLotById(id));
        assertTrue(ex.getMessage().contains("Unknown Lot Id"));
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(id);
    }

    // ==== CREATE ====
    @Test
    public void whenValidCreate_thenReturnCreatedDto() {
        // arrange request
        LotRequestModel req = new LotRequestModel();
        req.setCivicAddress("CreateLoc");
        req.setPrice(123f);
        req.setDimensionsSquareFeet("1230");
        req.setDimensionsSquareMeters("114.3");
        req.setLotStatus(LotStatus.AVAILABLE);

        // repository.save should return the entity (service sets LotIdentifier before save)
        when(lotRepository.save(any(Lot.class))).thenAnswer(invocation -> {
            Lot arg = invocation.getArgument(0);
            if (arg.getLotIdentifier() == null) {
                arg.setLotIdentifier(new LotIdentifier("gen-id"));
            }
            return arg;
        });

        // act
        LotResponseModel resp = lotService.addLot(req);

        // assert
        assertNotNull(resp);
        assertEquals("CreateLoc", resp.getCivicAddress());
        assertNotNull(resp.getLotId());
        verify(lotRepository, times(1)).save(any(Lot.class));
    }

    @Test
    public void whenCreateWithNullReq_thenThrowsNPEorIllegalArg() {
        // Negative: if caller passes null, behavior depends on implementation - assert it throws
        assertThrows(Exception.class, () -> lotService.addLot(null));
    }

    // ==== UPDATE ====
    @Test
    public void whenUpdateExisting_thenReturnUpdatedDto() {
        String id = "upd-id-1";
        var stored = buildLotEntity(id, "OldLoc", 50f, "500", "46.5", LotStatus.AVAILABLE);

        when(lotRepository.findByLotIdentifier_LotId(id)).thenReturn(stored);
        when(lotRepository.save(stored)).thenReturn(stored);

        LotRequestModel req = new LotRequestModel();
        req.setCivicAddress("NewLoc");
        req.setPrice(60f);
        req.setDimensionsSquareFeet("600");
        req.setDimensionsSquareMeters("55.7");
        req.setLotStatus(LotStatus.SOLD);

        LotResponseModel resp = lotService.updateLot(req, id);

        assertNotNull(resp);
        assertEquals("NewLoc", resp.getCivicAddress());
        assertEquals(LotStatus.SOLD, resp.getLotStatus());
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(id);
        verify(lotRepository, times(1)).save(stored);
    }

    @Test
    public void whenUpdateNonExisting_thenThrowNotFound() {
        String id = "missing-id";
        when(lotRepository.findByLotIdentifier_LotId(id)).thenReturn(null);
        LotRequestModel req = new LotRequestModel();
        assertThrows(NotFoundException.class, () -> lotService.updateLot(req, id));
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(id);
    }

    // ==== DELETE ====
    @Test
    public void whenDeleteExisting_thenDeletes() {
        String id = "del-id-1";
        var stored = buildLotEntity(id, "Rem", 10f, "100", "9.3", LotStatus.AVAILABLE);
        when(lotRepository.findByLotIdentifier_LotId(id)).thenReturn(stored);

        assertDoesNotThrow(() -> lotService.deleteLot(id));
        verify(lotRepository, times(1)).delete(stored);
    }

    @Test
    public void whenDeleteNonExisting_thenThrowNotFound() {
        String id = "not-exist";
        when(lotRepository.findByLotIdentifier_LotId(id)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> lotService.deleteLot(id));
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(id);
    }
}
