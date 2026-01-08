package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Realization.RealizationServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.Realization;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Realization.RealizationResponseModel;
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
public class RealizationServiceUnitTest {

    @Mock
    private RealizationRepository realizationRepository;

    @InjectMocks
    private RealizationServiceImpl realizationService;

    private Realization buildRealizationEntity(String realizationId, String realizationName, String location, String description,
                                   Integer numberOfRooms, Integer numberOfBedrooms, Integer numberOfBathrooms,
                                   Integer constructionYear) {
        Realization realization = new Realization();
        realization.setRealizationIdentifier(new RealizationIdentifier(realizationId));
        realization.setRealizationName(realizationName);
        realization.setLocation(location);
        realization.setDescription(description);
        realization.setNumberOfRooms(numberOfRooms);
        realization.setNumberOfBedrooms(numberOfBedrooms);
        realization.setNumberOfBathrooms(numberOfBathrooms);
        realization.setConstructionYear(constructionYear);
        return realization;
    }

    // ==== GET ALL ====
    @Test
    public void whenRealizationsExist_thenReturnAllRealizations() {
        // arrange
        var e1 = buildRealizationEntity("id-1", "Realization 1", "Location 1", "Desc 1", 5, 3, 2, 2020);
        var e2 = buildRealizationEntity("id-2", "Realization 2", "Location 2", "Desc 2", 6, 4, 3, 2021);
        when(realizationRepository.findAll()).thenReturn(List.of(e1, e2));

        // act
        List<RealizationResponseModel> list = realizationService.getAllRealizations();

        // assert
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Realization 1", list.get(0).getRealizationName());
        assertEquals("Location 1", list.get(0).getLocation());
        verify(realizationRepository, times(1)).findAll();
    }

    @Test
    public void whenNoRealizationsExist_thenReturnEmptyList() {
        when(realizationRepository.findAll()).thenReturn(List.of());

        List<RealizationResponseModel> list = realizationService.getAllRealizations();

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(realizationRepository, times(1)).findAll();
    }

    // ==== GET BY ID ====
    @Test
    public void whenGetByIdFound_thenReturnDto() {
        String id = "found-id-1";
        var entity = buildRealizationEntity(id, "Found Realization", "Found Loc", "Found Desc", 4, 2, 1, 2019);
        when(realizationRepository.findRealizationByRealizationIdentifier_RealizationId(id)).thenReturn(entity);

        RealizationResponseModel resp = realizationService.getRealizationById(id);

        assertNotNull(resp);
        assertEquals(id, resp.getRealizationId());
        assertEquals("Found Realization", resp.getRealizationName());
        assertEquals("Found Loc", resp.getLocation());
        assertEquals("Found Desc", resp.getDescription());
        assertEquals(4, resp.getNumberOfRooms());
        assertEquals(2, resp.getNumberOfBedrooms());
        assertEquals(1, resp.getNumberOfBathrooms());
        assertEquals(2019, resp.getConstructionYear());
        verify(realizationRepository, times(1)).findRealizationByRealizationIdentifier_RealizationId(id);
    }

    @Test
    public void whenGetByIdNotFound_thenThrowNotFound() {
        String id = "no-such-id";
        when(realizationRepository.findRealizationByRealizationIdentifier_RealizationId(id)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> realizationService.getRealizationById(id));
        assertTrue(ex.getMessage().contains("Unknown Realization Id"));
        verify(realizationRepository, times(1)).findRealizationByRealizationIdentifier_RealizationId(id);
    }
}
