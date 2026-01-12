package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
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
                .id(1)
                .scheduleIdentifier("SCH-001")
                .taskDate(LocalDate.now())
                .taskDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .build();

        schedule2 = Schedule.builder()
                .id(2)
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

    // Tests for addSchedule
    @Test
    void addSchedule_shouldCreateScheduleSuccessfully() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .build();

        Schedule newSchedule = Schedule.builder()
                .scheduleIdentifier("SCH-NEW")
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .build();

        ScheduleResponseDTO responseDTO = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-NEW")
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .build();

        when(scheduleMapper.requestDTOToEntity(requestDTO)).thenReturn(newSchedule);
        when(scheduleRepository.save(newSchedule)).thenReturn(newSchedule);
        when(scheduleMapper.entityToResponseDTO(newSchedule)).thenReturn(responseDTO);

        ScheduleResponseDTO result = scheduleService.addSchedule(requestDTO);

        assertNotNull(result);
        assertEquals("SCH-NEW", result.getScheduleIdentifier());
        assertEquals("New Task", result.getTaskDescription());

        verify(scheduleMapper).requestDTOToEntity(requestDTO);
        verify(scheduleRepository).save(newSchedule);
        verify(scheduleMapper).entityToResponseDTO(newSchedule);
    }

    @Test
    void addSchedule_shouldThrowExceptionWhenTaskDateIsNull() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .taskDate(null)
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .build();

        assertThrows(InvalidInputException.class, () -> {
            scheduleService.addSchedule(requestDTO);
        });
    }

    @Test
    void addSchedule_shouldThrowExceptionWhenTaskDescriptionIsBlank() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .taskDate(LocalDate.now())
                .taskDescription("")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .build();

        assertThrows(InvalidInputException.class, () -> {
            scheduleService.addSchedule(requestDTO);
        });
    }

    // Tests for updateSchedule
    @Test
    void updateSchedule_shouldUpdateScheduleSuccessfully() {
        String identifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .taskDate(LocalDate.now().plusDays(5))
                .taskDescription("Updated Task")
                .lotNumber("Lot 101")
                .dayOfWeek("Friday")
                .build();

        ScheduleResponseDTO updatedResponseDTO = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-001")
                .taskDate(LocalDate.now().plusDays(5))
                .taskDescription("Updated Task")
                .lotNumber("Lot 101")
                .dayOfWeek("Friday")
                .build();

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.of(schedule1));
        when(scheduleRepository.save(schedule1)).thenReturn(schedule1);
        when(scheduleMapper.entityToResponseDTO(schedule1)).thenReturn(updatedResponseDTO);

        ScheduleResponseDTO result = scheduleService.updateSchedule(identifier, requestDTO);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        assertEquals("Updated Task", result.getTaskDescription());

        verify(scheduleRepository).findByScheduleIdentifier(identifier);
        verify(scheduleMapper).updateEntityFromRequestDTO(schedule1, requestDTO);
        verify(scheduleRepository).save(schedule1);
    }

    @Test
    void updateSchedule_shouldThrowNotFoundExceptionWhenScheduleNotFound() {
        String identifier = "SCH-999";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .taskDate(LocalDate.now())
                .taskDescription("Updated Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Wednesday")
                .build();

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.updateSchedule(identifier, requestDTO);
        });
    }

    // Tests for deleteSchedule
    @Test
    void deleteSchedule_shouldDeleteScheduleSuccessfully() {
        String identifier = "SCH-001";

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.of(schedule1));

        scheduleService.deleteSchedule(identifier);

        verify(scheduleRepository).findByScheduleIdentifier(identifier);
        verify(scheduleRepository).delete(schedule1);
    }

    @Test
    void deleteSchedule_shouldThrowNotFoundExceptionWhenScheduleNotFound() {
        String identifier = "SCH-999";

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.deleteSchedule(identifier);
        });

        verify(scheduleRepository).findByScheduleIdentifier(identifier);
        verify(scheduleRepository, never()).delete(any());
    }

    // Tests for Task summary methods
    @Test
    void getCurrentWeekTaskSummary_shouldReturnTaskSummary() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);

        when(scheduleRepository.findCurrentWeekSchedules(startOfWeek, endOfWeek)).thenReturn(schedules);

        TaskResponseDTO result = scheduleService.getCurrentWeekTaskSummary("contractor-123");

        assertNotNull(result);
        assertEquals("contractor-123", result.getContractorId());
        assertEquals(startOfWeek, result.getPeriodStart());
        assertEquals(endOfWeek, result.getPeriodEnd());
        assertEquals(2, result.getTotalTasks());
        assertNotNull(result.getGeneratedAt());
        assertTrue(result.getMilestonesPresent());
    }

    @Test
    void getTaskSummaryForContractor_shouldReturnTaskSummaryForPeriod() {
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = LocalDate.now().plusDays(7);
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);

        when(scheduleRepository.findByTaskDateBetween(periodStart, periodEnd)).thenReturn(schedules);

        TaskResponseDTO result = scheduleService.getTaskSummaryForContractor(
                "contractor-456", "schedule-789", periodStart, periodEnd);

        assertNotNull(result);
        assertEquals("contractor-456", result.getContractorId());
        assertEquals("schedule-789", result.getScheduleId());
        assertEquals(periodStart, result.getPeriodStart());
        assertEquals(periodEnd, result.getPeriodEnd());
        assertEquals(2, result.getTotalTasks());
    }

    @Test
    void getTaskSummaryForContractor_shouldReturnEmptyTaskSummaryWhenNoSchedules() {
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = LocalDate.now().plusDays(7);

        when(scheduleRepository.findByTaskDateBetween(periodStart, periodEnd)).thenReturn(Collections.emptyList());

        TaskResponseDTO result = scheduleService.getTaskSummaryForContractor(
                "contractor-456", "schedule-789", periodStart, periodEnd);

        assertNotNull(result);
        assertEquals(0, result.getTotalTasks());
        assertEquals(0, result.getOpenTasksCount());
        assertEquals(0, result.getCompletedTasksCount());
        assertFalse(result.getMilestonesPresent());
    }
}