package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Renovation.RenovationSericeImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Renovation.RenovationResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    // ==== CREATE RENOVATION ====
    @Test
    public void whenCreateRenovationWithValidData_thenReturnCreatedRenovation() {
        // arrange
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription("Test renovation description");

        Renovation savedRenovation = new Renovation();
        savedRenovation.setRenovationIdentifier(new RenovationIdentifier("generated-uuid"));
        savedRenovation.setBeforeImageIdentifier("before-123");
        savedRenovation.setAfterImageIdentifier("after-456");
        savedRenovation.setDescription("Test renovation description");

        ArgumentCaptor<Renovation> renovationCaptor = ArgumentCaptor.forClass(Renovation.class);
        when(renovationRepository.save(any(Renovation.class))).thenReturn(savedRenovation);

        // act
        RenovationResponseModel response = renovationService.createRenovation(requestModel);

        // assert
        assertNotNull(response);
        assertNotNull(response.getRenovationId());
        assertEquals("before-123", response.getBeforeImageIdentifier());
        assertEquals("after-456", response.getAfterImageIdentifier());
        assertEquals("Test renovation description", response.getDescription());
        
        verify(renovationRepository, times(1)).save(renovationCaptor.capture());
        Renovation capturedRenovation = renovationCaptor.getValue();
        assertEquals("before-123", capturedRenovation.getBeforeImageIdentifier());
        assertEquals("after-456", capturedRenovation.getAfterImageIdentifier());
        assertEquals("Test renovation description", capturedRenovation.getDescription());
        assertNotNull(capturedRenovation.getRenovationIdentifier());
    }

    @Test
    public void whenCreateRenovationWithNullBeforeImageIdentifier_thenThrowIllegalArgumentException() {
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier(null);
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription("Test description");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.createRenovation(requestModel));
        assertTrue(ex.getMessage().contains("Before Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenCreateRenovationWithEmptyBeforeImageIdentifier_thenThrowIllegalArgumentException() {
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("");
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription("Test description");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.createRenovation(requestModel));
        assertTrue(ex.getMessage().contains("Before Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenCreateRenovationWithNullAfterImageIdentifier_thenThrowIllegalArgumentException() {
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier(null);
        requestModel.setDescription("Test description");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.createRenovation(requestModel));
        assertTrue(ex.getMessage().contains("After Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenCreateRenovationWithEmptyAfterImageIdentifier_thenThrowIllegalArgumentException() {
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier("");
        requestModel.setDescription("Test description");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.createRenovation(requestModel));
        assertTrue(ex.getMessage().contains("After Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenCreateRenovationWithNullDescription_thenThrowIllegalArgumentException() {
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.createRenovation(requestModel));
        assertTrue(ex.getMessage().contains("Description cannot be empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenCreateRenovationWithEmptyDescription_thenThrowIllegalArgumentException() {
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.createRenovation(requestModel));
        assertTrue(ex.getMessage().contains("Description cannot be empty"));
        verify(renovationRepository, never()).save(any());
    }

    // ==== UPDATE RENOVATION ====
    @Test
    public void whenUpdateRenovationWithValidData_thenReturnUpdatedRenovation() {
        // arrange
        String renovationId = "existing-id-123";
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("updated-before");
        requestModel.setAfterImageIdentifier("updated-after");
        requestModel.setDescription("Updated description");

        Renovation existingRenovation = buildRenovationEntity(renovationId, "old-before", "old-after", "Old description");
        Renovation updatedRenovation = buildRenovationEntity(renovationId, "updated-before", "updated-after", "Updated description");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);
        when(renovationRepository.save(existingRenovation)).thenReturn(updatedRenovation);

        // act
        RenovationResponseModel response = renovationService.updateRenovation(requestModel, renovationId);

        // assert
        assertNotNull(response);
        assertEquals(renovationId, response.getRenovationId());
        assertEquals("updated-before", response.getBeforeImageIdentifier());
        assertEquals("updated-after", response.getAfterImageIdentifier());
        assertEquals("Updated description", response.getDescription());
        
        verify(renovationRepository, times(1)).findRenovationByRenovationIdentifier_RenovationId(renovationId);
        verify(renovationRepository, times(1)).save(existingRenovation);
        assertEquals("updated-before", existingRenovation.getBeforeImageIdentifier());
        assertEquals("updated-after", existingRenovation.getAfterImageIdentifier());
        assertEquals("Updated description", existingRenovation.getDescription());
    }

    @Test
    public void whenUpdateRenovationNotFound_thenThrowNotFoundException() {
        String renovationId = "non-existent-id";
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before");
        requestModel.setAfterImageIdentifier("after");
        requestModel.setDescription("Description");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, 
            () -> renovationService.updateRenovation(requestModel, renovationId));
        assertTrue(ex.getMessage().contains("Unknown Renovation Id"));
        verify(renovationRepository, times(1)).findRenovationByRenovationIdentifier_RenovationId(renovationId);
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenUpdateRenovationWithNullBeforeImageIdentifier_thenThrowIllegalArgumentException() {
        String renovationId = "existing-id";
        Renovation existingRenovation = buildRenovationEntity(renovationId, "old-before", "old-after", "Old description");
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier(null);
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription("Test description");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.updateRenovation(requestModel, renovationId));
        assertTrue(ex.getMessage().contains("Before Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenUpdateRenovationWithEmptyBeforeImageIdentifier_thenThrowIllegalArgumentException() {
        String renovationId = "existing-id";
        Renovation existingRenovation = buildRenovationEntity(renovationId, "old-before", "old-after", "Old description");
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("");
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription("Test description");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.updateRenovation(requestModel, renovationId));
        assertTrue(ex.getMessage().contains("Before Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenUpdateRenovationWithNullAfterImageIdentifier_thenThrowIllegalArgumentException() {
        String renovationId = "existing-id";
        Renovation existingRenovation = buildRenovationEntity(renovationId, "old-before", "old-after", "Old description");
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier(null);
        requestModel.setDescription("Test description");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.updateRenovation(requestModel, renovationId));
        assertTrue(ex.getMessage().contains("After Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenUpdateRenovationWithEmptyAfterImageIdentifier_thenThrowIllegalArgumentException() {
        String renovationId = "existing-id";
        Renovation existingRenovation = buildRenovationEntity(renovationId, "old-before", "old-after", "Old description");
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier("");
        requestModel.setDescription("Test description");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.updateRenovation(requestModel, renovationId));
        assertTrue(ex.getMessage().contains("After Image Identifier cannot be null or empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenUpdateRenovationWithNullDescription_thenThrowIllegalArgumentException() {
        String renovationId = "existing-id";
        Renovation existingRenovation = buildRenovationEntity(renovationId, "old-before", "old-after", "Old description");
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription(null);

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.updateRenovation(requestModel, renovationId));
        assertTrue(ex.getMessage().contains("Description cannot be empty"));
        verify(renovationRepository, never()).save(any());
    }

    @Test
    public void whenUpdateRenovationWithEmptyDescription_thenThrowIllegalArgumentException() {
        String renovationId = "existing-id";
        Renovation existingRenovation = buildRenovationEntity(renovationId, "old-before", "old-after", "Old description");
        RenovationRequestModel requestModel = new RenovationRequestModel();
        requestModel.setBeforeImageIdentifier("before-123");
        requestModel.setAfterImageIdentifier("after-456");
        requestModel.setDescription("");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> renovationService.updateRenovation(requestModel, renovationId));
        assertTrue(ex.getMessage().contains("Description cannot be empty"));
        verify(renovationRepository, never()).save(any());
    }

    // ==== DELETE RENOVATION ====
    @Test
    public void whenDeleteRenovationFound_thenDeleteSuccessfully() {
        String renovationId = "existing-id-123";
        Renovation existingRenovation = buildRenovationEntity(renovationId, "before", "after", "Description");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(existingRenovation);
        doNothing().when(renovationRepository).delete(existingRenovation);

        // act
        renovationService.deleteRenovation(renovationId);

        // assert
        verify(renovationRepository, times(1)).findRenovationByRenovationIdentifier_RenovationId(renovationId);
        verify(renovationRepository, times(1)).delete(existingRenovation);
    }

    @Test
    public void whenDeleteRenovationNotFound_thenThrowNotFoundException() {
        String renovationId = "non-existent-id";
        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(renovationId))
            .thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, 
            () -> renovationService.deleteRenovation(renovationId));
        assertTrue(ex.getMessage().contains("Unknown Renovation Id"));
        verify(renovationRepository, times(1)).findRenovationByRenovationIdentifier_RenovationId(renovationId);
        verify(renovationRepository, never()).delete(any());
    }

    // ==== MAP TO RESPONSE ====
    @Test
    public void whenMapToResponse_thenReturnCorrectResponseModel() {
        // This test verifies mapToResponse through getAllRenovations
        Renovation renovation = buildRenovationEntity("test-id", "before-img", "after-img", "Test description");
        when(renovationRepository.findAll()).thenReturn(List.of(renovation));

        List<RenovationResponseModel> response = renovationService.getAllRenovations();

        assertEquals(1, response.size());
        RenovationResponseModel model = response.get(0);
        assertEquals("test-id", model.getRenovationId());
        assertEquals("before-img", model.getBeforeImageIdentifier());
        assertEquals("after-img", model.getAfterImageIdentifier());
        assertEquals("Test description", model.getDescription());
    }
}
