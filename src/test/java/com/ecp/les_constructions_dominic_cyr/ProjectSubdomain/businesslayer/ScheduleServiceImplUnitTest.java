package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplUnitTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private Schedule schedule1;
    private Schedule schedule2;
    private ScheduleResponseDTO responseDTO1;
    private ScheduleResponseDTO responseDTO2;

    @BeforeEach
    void setUp() {
        schedule1 = Schedule.builder()
                .id(1L)
                .scheduleIdentifier("SCH-001")
                .taskDate(LocalDate.now())
                .taskDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .build();

        schedule2 = Schedule.builder()
                .id(2L)
                .scheduleIdentifier("SCH-002")
                .taskDate(LocalDate.now().plusDays(1))
                .taskDescription("Plumbing")
                .lotNumber("Lot 57")
                .dayOfWeek("Tuesday")
                .build();

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
    void getCurrentWeekSchedules_shouldReturnSchedulesForCurrentWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);
        List<ScheduleResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(scheduleRepository.findCurrentWeekSchedules(startOfWeek, endOfWeek)).thenReturn(schedules);
        when(scheduleMapper.entitiesToResponseDTOs(schedules)).thenReturn(responseDTOs);

        List<ScheduleResponseDTO> result = scheduleService.getCurrentWeekSchedules();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("SCH-001", result.get(0).getScheduleIdentifier());
        assertEquals("SCH-002", result.get(1).getScheduleIdentifier());

        verify(scheduleRepository).findCurrentWeekSchedules(startOfWeek, endOfWeek);
        verify(scheduleMapper).entitiesToResponseDTOs(schedules);
    }

    @Test
    void getCurrentWeekSchedules_shouldReturnEmptyListWhenNoSchedules() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        when(scheduleRepository.findCurrentWeekSchedules(startOfWeek, endOfWeek)).thenReturn(Collections.emptyList());
        when(scheduleMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ScheduleResponseDTO> result = scheduleService.getCurrentWeekSchedules();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(scheduleRepository).findCurrentWeekSchedules(startOfWeek, endOfWeek);
        verify(scheduleMapper).entitiesToResponseDTOs(Collections.emptyList());
    }

    @Test
    void getAllSchedules_shouldReturnAllSchedules() {
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);
        List<ScheduleResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(scheduleRepository.findAll()).thenReturn(schedules);
        when(scheduleMapper.entitiesToResponseDTOs(schedules)).thenReturn(responseDTOs);

        List<ScheduleResponseDTO> result = scheduleService.getAllSchedules();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("SCH-001", result.get(0).getScheduleIdentifier());
        assertEquals("SCH-002", result.get(1).getScheduleIdentifier());

        verify(scheduleRepository).findAll();
        verify(scheduleMapper).entitiesToResponseDTOs(schedules);
    }

    @Test
    void getAllSchedules_shouldReturnEmptyListWhenNoSchedules() {
        when(scheduleRepository.findAll()).thenReturn(Collections.emptyList());
        when(scheduleMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ScheduleResponseDTO> result = scheduleService.getAllSchedules();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(scheduleRepository).findAll();
        verify(scheduleMapper).entitiesToResponseDTOs(Collections.emptyList());
    }

    @Test
    void getScheduleByIdentifier_shouldReturnScheduleWhenFound() {
        String identifier = "SCH-001";

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.of(schedule1));
        when(scheduleMapper.entityToResponseDTO(schedule1)).thenReturn(responseDTO1);

        ScheduleResponseDTO result = scheduleService.getScheduleByIdentifier(identifier);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        assertEquals("Begin Excavation", result.getTaskDescription());
        assertEquals("Lot 53", result.getLotNumber());

        verify(scheduleRepository).findByScheduleIdentifier(identifier);
        verify(scheduleMapper).entityToResponseDTO(schedule1);
    }

    @Test
    void getScheduleByIdentifier_shouldThrowExceptionWhenNotFound() {
        String identifier = "SCH-999";

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scheduleService.getScheduleByIdentifier(identifier);
        });

        assertEquals("Schedule not found with identifier: SCH-999", exception.getMessage());

        verify(scheduleRepository).findByScheduleIdentifier(identifier);
        verify(scheduleMapper, never()).entityToResponseDTO(any());
    }
}