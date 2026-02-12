package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplUnitTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private Schedule schedule1;
    private Schedule schedule2;
    private ScheduleResponseDTO responseDTO1;
    private ScheduleResponseDTO responseDTO2;
    private Lot lot;
    private Project project;

    private static final String LOT_53_ID = UUID.randomUUID().toString();
    private static final String LOT_57_ID = UUID.randomUUID().toString();
    private static final String LOT_100_ID = UUID.randomUUID().toString();
    private static final String LOT_101_ID = UUID.randomUUID().toString();
    private static final String LOT_999_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        LotIdentifier lotIdentifier = new LotIdentifier(LOT_53_ID);
        lot = new Lot(lotIdentifier, "Lot-53", "Test Address", 150000f, "5000", "464.5", LotStatus.AVAILABLE);
        lot.setId(1);

        project = new Project();
        project.setProjectId(1L);
        project.setProjectIdentifier("proj-001");
        project.setProjectName("Test Project");

        schedule1 = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Begin Excavation")
                .lotId(UUID.fromString(LOT_53_ID))
                .project(project)
                .build();

        schedule2 = Schedule.builder()
                .id(2)
                .scheduleIdentifier("SCH-002")
                .scheduleStartDate(LocalDate.now().plusDays(1))
                .scheduleEndDate(LocalDate.now().plusDays(1))
                .scheduleDescription("Plumbing")
                .lotId(UUID.fromString(LOT_57_ID))
                .project(project)
                .build();

        responseDTO1 = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Begin Excavation")
                .lotId(LOT_53_ID)
                .build();

        responseDTO2 = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-002")
                .scheduleStartDate(LocalDate.now().plusDays(1))
                .scheduleEndDate(LocalDate.now().plusDays(1))
                .scheduleDescription("Plumbing")
                .lotId(LOT_57_ID)
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
        assertEquals("Begin Excavation", result.getScheduleDescription());
        assertEquals(LOT_53_ID, result.getLotId());

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
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("New Task")
                .lotId(LOT_100_ID)
                .build();

        LotIdentifier lotId100 = new LotIdentifier(LOT_100_ID);
        Lot lot100 = new Lot();
        lot100.setId(2);
        lot100.setLotIdentifier(lotId100);

        Schedule newSchedule = Schedule.builder()
                .scheduleIdentifier("SCH-NEW")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("New Task")
                .lotId(UUID.fromString(LOT_100_ID))
                .build();

        ScheduleResponseDTO responseDTO = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-NEW")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("New Task")
                .lotId(LOT_100_ID)
                .build();

        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(LOT_100_ID))).thenReturn(lot100);
        when(scheduleMapper.requestDTOToEntity(requestDTO)).thenReturn(newSchedule);
        when(scheduleRepository.save(newSchedule)).thenReturn(newSchedule);
        when(scheduleMapper.entityToResponseDTO(newSchedule)).thenReturn(responseDTO);

        ScheduleResponseDTO result = scheduleService.addSchedule(requestDTO);

        assertNotNull(result);
        assertEquals("SCH-NEW", result.getScheduleIdentifier());
        assertEquals("New Task", result.getScheduleDescription());

        verify(lotRepository).findByLotIdentifier_LotId(UUID.fromString(LOT_100_ID));
        verify(scheduleMapper).requestDTOToEntity(requestDTO);
        verify(scheduleRepository).save(newSchedule);
        verify(scheduleMapper).entityToResponseDTO(newSchedule);
    }

    @Test
    void addSchedule_shouldThrowExceptionWhenScheduleStartDateIsNull() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(null)
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("New Task")
                .lotId(LOT_100_ID)
                .build();

        assertThrows(InvalidInputException.class, () -> {
            scheduleService.addSchedule(requestDTO);
        });
    }

    @Test
    void addSchedule_shouldThrowExceptionWhenScheduleDescriptionIsBlank() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("")
                .lotId(LOT_100_ID)
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
                .scheduleStartDate(LocalDate.now().plusDays(5))
                .scheduleEndDate(LocalDate.now().plusDays(5))
                .scheduleDescription("Updated Task")
                .lotId(LOT_101_ID)
                .build();

        LotIdentifier lotId101 = new LotIdentifier(LOT_101_ID);
        Lot lot101 = new Lot();
        lot101.setId(3);
        lot101.setLotIdentifier(lotId101);

        ScheduleResponseDTO updatedResponseDTO = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.now().plusDays(5))
                .scheduleEndDate(LocalDate.now().plusDays(5))
                .scheduleDescription("Updated Task")
                .lotId(LOT_101_ID)
                .build();

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.of(schedule1));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(LOT_101_ID))).thenReturn(lot101);
        when(scheduleRepository.save(schedule1)).thenReturn(schedule1);
        when(scheduleMapper.entityToResponseDTO(schedule1)).thenReturn(updatedResponseDTO);

        ScheduleResponseDTO result = scheduleService.updateSchedule(identifier, requestDTO);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        assertEquals("Updated Task", result.getScheduleDescription());

        verify(scheduleRepository).findByScheduleIdentifier(identifier);
        verify(lotRepository).findByLotIdentifier_LotId(UUID.fromString(LOT_101_ID));
        verify(scheduleMapper).updateEntityFromRequestDTO(schedule1, requestDTO);
        verify(scheduleRepository).save(schedule1);
    }

    @Test
    void updateSchedule_shouldThrowNotFoundExceptionWhenScheduleNotFound() {
        String identifier = "SCH-999";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Updated Task")
                .lotId(LOT_100_ID)
                .build();

        LotIdentifier lotId100 = new LotIdentifier(LOT_100_ID);
        Lot lot100 = new Lot();
        lot100.setId(2);
        lot100.setLotIdentifier(lotId100);

        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(LOT_100_ID))).thenReturn(lot100);
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

        when(scheduleRepository.findByScheduleStartDateBetween(periodStart, periodEnd)).thenReturn(schedules);

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

        when(scheduleRepository.findByScheduleStartDateBetween(periodStart, periodEnd)).thenReturn(Collections.emptyList());

        TaskResponseDTO result = scheduleService.getTaskSummaryForContractor(
                "contractor-456", "schedule-789", periodStart, periodEnd);

        assertNotNull(result);
        assertEquals(0, result.getTotalTasks());
        assertEquals(0, result.getOpenTasksCount());
        assertEquals(0, result.getCompletedTasksCount());
        assertFalse(result.getMilestonesPresent());
    }

    // Tests for validation methods
    @Test
    void addSchedule_shouldThrowExceptionWhenScheduleEndDateIsNull() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(null)
                .scheduleDescription("Test")
                .lotId(LOT_100_ID)
                .build();

        assertThrows(InvalidInputException.class, () -> {
            scheduleService.addSchedule(requestDTO);
        });
    }

    @Test
    void addSchedule_shouldThrowExceptionWhenLotIdIsBlank() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Test")
                .lotId("")
                .build();

        assertThrows(InvalidInputException.class, () -> {
            scheduleService.addSchedule(requestDTO);
        });
    }

    @Test
    void addSchedule_shouldThrowNotFoundExceptionWhenLotNotFound() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Test")
                .lotId(LOT_999_ID)
                .build();

        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(LOT_999_ID))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            scheduleService.addSchedule(requestDTO);
        });

        verify(lotRepository).findByLotIdentifier_LotId(UUID.fromString(LOT_999_ID));
    }

    @Test
    void updateSchedule_shouldUpdateWithLotRepository() {
        String identifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now().plusDays(5))
                .scheduleEndDate(LocalDate.now().plusDays(5))
                .scheduleDescription("Updated Task")
                .lotId(LOT_101_ID)
                .build();

        LotIdentifier lotId101 = new LotIdentifier(LOT_101_ID);
        Lot lot101 = new Lot();
        lot101.setId(3);
        lot101.setLotIdentifier(lotId101);

        ScheduleResponseDTO updatedResponseDTO = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.now().plusDays(5))
                .scheduleEndDate(LocalDate.now().plusDays(5))
                .scheduleDescription("Updated Task")
                .lotId(LOT_101_ID)
                .build();

        when(scheduleRepository.findByScheduleIdentifier(identifier)).thenReturn(Optional.of(schedule1));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(LOT_101_ID))).thenReturn(lot101);
        when(scheduleRepository.save(schedule1)).thenReturn(schedule1);
        when(scheduleMapper.entityToResponseDTO(schedule1)).thenReturn(updatedResponseDTO);

        ScheduleResponseDTO result = scheduleService.updateSchedule(identifier, requestDTO);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        assertEquals("Updated Task", result.getScheduleDescription());

        verify(scheduleRepository).findByScheduleIdentifier(identifier);
        verify(lotRepository).findByLotIdentifier_LotId(UUID.fromString(LOT_101_ID));
        verify(scheduleMapper).updateEntityFromRequestDTO(schedule1, requestDTO);
        verify(scheduleRepository).save(schedule1);
    }

    // Tests for project-related methods
    @Test
    void getSchedulesByProjectIdentifier_shouldReturnSchedulesForProject() {
        String projectIdentifier = "proj-001";
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);
        List<ScheduleResponseDTO> responseDTOs = Arrays.asList(responseDTO1, responseDTO2);

        when(scheduleRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(schedules);
        when(scheduleMapper.entitiesToResponseDTOs(schedules)).thenReturn(responseDTOs);

        List<ScheduleResponseDTO> result = scheduleService.getSchedulesByProjectIdentifier(projectIdentifier);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(scheduleRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleMapper).entitiesToResponseDTOs(schedules);
    }

    @Test
    void getSchedulesByProjectIdentifier_shouldReturnEmptyListWhenNoSchedules() {
        String projectIdentifier = "proj-999";

        when(scheduleRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Collections.emptyList());
        when(scheduleMapper.entitiesToResponseDTOs(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ScheduleResponseDTO> result = scheduleService.getSchedulesByProjectIdentifier(projectIdentifier);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(scheduleRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleMapper).entitiesToResponseDTOs(Collections.emptyList());
    }

    @Test
    void getScheduleByProjectAndScheduleIdentifier_shouldReturnScheduleWhenFound() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)).thenReturn(Optional.of(schedule1));
        when(scheduleMapper.entityToResponseDTO(schedule1)).thenReturn(responseDTO1);

        ScheduleResponseDTO result = scheduleService.getScheduleByProjectAndScheduleIdentifier(
                projectIdentifier, scheduleIdentifier);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository).findByScheduleIdentifier(scheduleIdentifier);
        verify(scheduleMapper).entityToResponseDTO(schedule1);
    }

    @Test
    void getScheduleByProjectAndScheduleIdentifier_shouldThrowNotFoundWhenProjectNotFound() {
        String projectIdentifier = "proj-999";
        String scheduleIdentifier = "SCH-001";

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier);
        });

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository, never()).findByScheduleIdentifier(any());
    }

    @Test
    void getScheduleByProjectAndScheduleIdentifier_shouldThrowNotFoundWhenScheduleNotFound() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-999";

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier);
        });

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository).findByScheduleIdentifier(scheduleIdentifier);
    }

    @Test
    void getScheduleByProjectAndScheduleIdentifier_shouldThrowNotFoundWhenScheduleDoesNotBelongToProject() {
        String projectIdentifier = "proj-002";
        String scheduleIdentifier = "SCH-001";

        Project otherProject = new Project();
        otherProject.setProjectId(2L);
        otherProject.setProjectIdentifier("proj-002");
        otherProject.setProjectName("Other Project");

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(otherProject));
        when(scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)).thenReturn(Optional.of(schedule1));

        assertThrows(NotFoundException.class, () -> {
            scheduleService.getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier);
        });

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository).findByScheduleIdentifier(scheduleIdentifier);
    }

    @Test
    void addScheduleToProject_shouldCreateScheduleForProject() {
        String projectIdentifier = "proj-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("Project Schedule")
            .lotId(LOT_100_ID)
                .build();

        LotIdentifier lotId100 = new LotIdentifier(LOT_100_ID);
        Lot lot100 = new Lot();
        lot100.setId(2);
        lot100.setLotIdentifier(lotId100);

        Schedule newSchedule = Schedule.builder()
                .scheduleIdentifier("SCH-NEW")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("Project Schedule")
                .lotId(UUID.fromString(LOT_100_ID))
                .project(project)
                .build();

        ScheduleResponseDTO responseDTO = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-NEW")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("Project Schedule")
                .lotId(LOT_100_ID)
                .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(LOT_100_ID))).thenReturn(lot100);
        when(scheduleMapper.requestDTOToEntity(requestDTO)).thenReturn(newSchedule);
        when(scheduleRepository.save(newSchedule)).thenReturn(newSchedule);
        when(scheduleMapper.entityToResponseDTO(newSchedule)).thenReturn(responseDTO);

        ScheduleResponseDTO result = scheduleService.addScheduleToProject(projectIdentifier, requestDTO);

        assertNotNull(result);
        assertEquals("SCH-NEW", result.getScheduleIdentifier());

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(lotRepository).findByLotIdentifier_LotId(UUID.fromString(LOT_100_ID));
        verify(scheduleMapper).requestDTOToEntity(requestDTO);
        verify(scheduleRepository).save(newSchedule);
        verify(scheduleMapper).entityToResponseDTO(newSchedule);
    }

    @Test
    void addScheduleToProject_shouldThrowNotFoundWhenProjectNotFound() {
        String projectIdentifier = "proj-999";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("Project Schedule")
            .lotId(LOT_100_ID)
                .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.addScheduleToProject(projectIdentifier, requestDTO);
        });

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void updateScheduleForProject_shouldUpdateScheduleSuccessfully() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now().plusDays(2))
                .scheduleEndDate(LocalDate.now().plusDays(9))
                .scheduleDescription("Updated Project Schedule")
                .lotId(LOT_101_ID)
                .build();

        LotIdentifier lotId101 = new LotIdentifier(LOT_101_ID);
        Lot lot101 = new Lot();
        lot101.setId(3);
        lot101.setLotIdentifier(lotId101);

        ScheduleResponseDTO updatedResponseDTO = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.now().plusDays(2))
                .scheduleEndDate(LocalDate.now().plusDays(9))
                .scheduleDescription("Updated Project Schedule")
                .lotId(LOT_101_ID)
                .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(LOT_101_ID))).thenReturn(lot101);
        when(scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)).thenReturn(Optional.of(schedule1));
        when(scheduleRepository.save(schedule1)).thenReturn(schedule1);
        when(scheduleMapper.entityToResponseDTO(schedule1)).thenReturn(updatedResponseDTO);

        ScheduleResponseDTO result = scheduleService.updateScheduleForProject(
                projectIdentifier, scheduleIdentifier, requestDTO);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(lotRepository).findByLotIdentifier_LotId(UUID.fromString(LOT_101_ID));
        verify(scheduleRepository).findByScheduleIdentifier(scheduleIdentifier);
        verify(scheduleMapper).updateEntityFromRequestDTO(schedule1, requestDTO);
        verify(scheduleRepository).save(schedule1);
    }

    @Test
    void updateScheduleForProject_shouldThrowNotFoundWhenProjectNotFound() {
        String projectIdentifier = "proj-999";
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("Updated Schedule")
            .lotId(LOT_100_ID)
                .build();

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO);
        });

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository, never()).findByScheduleIdentifier(any());
    }

    @Test
    void deleteScheduleFromProject_shouldDeleteScheduleSuccessfully() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)).thenReturn(Optional.of(schedule1));

        scheduleService.deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository).findByScheduleIdentifier(scheduleIdentifier);
        verify(scheduleRepository).delete(schedule1);
    }

    @Test
    void deleteScheduleFromProject_shouldThrowNotFoundWhenProjectNotFound() {
        String projectIdentifier = "proj-999";
        String scheduleIdentifier = "SCH-001";

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);
        });

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository, never()).delete(any());
    }

    @Test
    void deleteScheduleFromProject_shouldThrowNotFoundWhenScheduleNotFound() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-999";

        when(projectRepository.findByProjectIdentifier(projectIdentifier)).thenReturn(Optional.of(project));
        when(scheduleRepository.findByScheduleIdentifier(scheduleIdentifier)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            scheduleService.deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);
        });

        verify(projectRepository).findByProjectIdentifier(projectIdentifier);
        verify(scheduleRepository).findByScheduleIdentifier(scheduleIdentifier);
        verify(scheduleRepository, never()).delete(any());
    }
}
