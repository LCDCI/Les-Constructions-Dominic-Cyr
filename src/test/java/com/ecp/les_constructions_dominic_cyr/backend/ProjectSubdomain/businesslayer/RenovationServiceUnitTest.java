package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Renovation.RenovationSericeImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationResponseModel;
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
public class RenovationServiceUnitTest {

    @Mock
    private RenovationRepository renovationRepository;

    @InjectMocks
    private RenovationSericeImpl renovationService;

    private Renovation buildRenovationEntity(String renovationId, String beforeImageId, String afterImageId, String description) {
        Renovation renovation = new Renovation();
        renovation.setRenovationIdentifier(new RenovationIdentifier(renovationId));
        renovation.setBeforeImageIdentifier(beforeImageId);
        renovation.setAfterImageIdentifier(afterImageId);
        renovation.setDescription(description);
        return renovation;
    }

    // ==== GET ALL ====
    @Test
    public void whenRenovationsExist_thenReturnAllRenovations() {
        // arrange
        var e1 = buildRenovationEntity("id-1", "before-1", "after-1", "Description 1");
        var e2 = buildRenovationEntity("id-2", "before-2", "after-2", "Description 2");
        when(renovationRepository.findAll()).thenReturn(List.of(e1, e2));

        // act
        List<RenovationResponseModel> list = renovationService.getAllRenovations();

        // assert
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Description 1", list.get(0).getDescription());
        assertEquals("before-1", list.get(0).getBeforeImageIdentifier());
        verify(renovationRepository, times(1)).findAll();
    }

    @Test
    public void whenNoRenovationsExist_thenReturnEmptyList() {
        when(renovationRepository.findAll()).thenReturn(List.of());

        List<RenovationResponseModel> list = renovationService.getAllRenovations();

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(renovationRepository, times(1)).findAll();
    }

    // ==== GET BY ID ====
    @Test
    public void whenGetByIdFound_thenReturnDto() {
        String id = "found-id-1";
        var entity = buildRenovationEntity(id, "before-found", "after-found", "Found Description");
        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(id)).thenReturn(entity);

        RenovationResponseModel resp = renovationService.getRenovationById(id);

        assertNotNull(resp);
        assertEquals(id, resp.getRenovationId());
        assertEquals("before-found", resp.getBeforeImageIdentifier());
        assertEquals("after-found", resp.getAfterImageIdentifier());
        assertEquals("Found Description", resp.getDescription());
        verify(renovationRepository, times(1)).findRenovationByRenovationIdentifier_RenovationId(id);
    }

    @Test
    public void whenGetByIdNotFound_thenThrowNotFound() {
        String id = "no-such-id";
        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(id)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> renovationService.getRenovationById(id));
        assertTrue(ex.getMessage().contains("Unknown Renovation Id"));
        verify(renovationRepository, times(1)).findRenovationByRenovationIdentifier_RenovationId(id);
    }
}
