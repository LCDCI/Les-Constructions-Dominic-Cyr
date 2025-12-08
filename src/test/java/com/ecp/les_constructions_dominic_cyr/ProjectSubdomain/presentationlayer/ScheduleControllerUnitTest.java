package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
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
class ScheduleControllerUnitTest {

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
    void getOwnerScheduleByIdentifier_shouldThrowExceptionWhenNotFound() {
        String identifier = "SCH-999";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new RuntimeException("Schedule not found with identifier: SCH-999"));

        assertThrows(RuntimeException.class, () -> {
            scheduleController.getOwnerScheduleByIdentifier(identifier);
        });

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("SCH-001", response.getBody().get(0).getScheduleIdentifier());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllSalespersonSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getAllSalespersonSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getAllSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());
        assertEquals("Begin Excavation", response.getBody().getTaskDescription());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldThrowExceptionWhenNotFound() {
        String identifier = "SCH-999";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new RuntimeException("Schedule not found with identifier: SCH-999"));

        assertThrows(RuntimeException.class, () -> {
            scheduleController.getSalespersonScheduleByIdentifier(identifier);
        });

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }
}