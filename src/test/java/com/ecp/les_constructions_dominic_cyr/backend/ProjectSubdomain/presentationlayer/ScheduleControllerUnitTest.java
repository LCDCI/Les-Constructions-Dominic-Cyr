package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.BadRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerUnitTest{

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController scheduleController;

    private ScheduleResponseDTO responseDTO1;
    private ScheduleResponseDTO responseDTO2;

    @BeforeEach
    void setUp() {
        responseDTO1 = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-001")
                .taskDate(LocalDate.now())
                .taskDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .build();

        responseDTO2 = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-002")
                .taskDate(LocalDate.now().plusDays(1))
                .taskDescription("Plumbing")
                .lotNumber("Lot 57")
                .dayOfWeek("Tuesday")
                .build();
    }

    // Owner Endpoints Tests
    @Test
    void getOwnerCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("SCH-001", response.getBody().get(0).getScheduleIdentifier());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getOwnerCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllOwnerSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getAllOwnerSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getAllSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());
        assertEquals("Begin Excavation", response.getBody().getTaskDescription());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Schedule not found with identifier: SCH-999";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid schedule identifier format";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID-FORMAT";
        String errorMessage = "Invalid request format";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    // Contractor Endpoints Tests
    @Test
    void getContractorCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getContractorCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getContractorCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getContractorCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllContractorSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getContractorAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Schedule not found";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Bad request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    // Salesperson Endpoints Tests
    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllSalespersonSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Not found";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Bad request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    // Customer Endpoints Tests
    @Test
    void getCustomerCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getCustomerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getCustomerCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getCustomerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllCustomerSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getCustomerAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Not found";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Bad request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }
}