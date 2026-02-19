package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)

class ScheduleMapperUnitTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private LotRepository lotRepository;

    private ScheduleMapper scheduleMapper;
    private TaskMapper taskMapper;
    private Schedule schedule1;
    private Schedule schedule2;
    private static final UUID LOT_53_UUID = UUID.randomUUID();
    private static final UUID LOT_57_UUID = UUID.randomUUID();
    private static final UUID LOT_100_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper(scheduleRepository, lotRepository);
        scheduleMapper = new ScheduleMapper(taskMapper);

        schedule1 = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.of(2024, 11, 26))
                .scheduleEndDate(LocalDate.of(2024, 11, 26))
                .scheduleDescription("Begin Excavation")
                .lotId(LOT_53_UUID)
                .tasks(new ArrayList<>())
                .build();

        schedule2 = Schedule.builder()
                .id(2)
                .scheduleIdentifier("SCH-002")
                .scheduleStartDate(LocalDate.of(2024, 11, 27))
                .scheduleEndDate(LocalDate.of(2024, 11, 27))
                .scheduleDescription("Plumbing")
                .lotId(LOT_57_UUID)
                .tasks(new ArrayList<>())
                .build();
    }

    @Test
    void entityToResponseDTO_shouldMapAllFieldsCorrectly() {
        ScheduleResponseDTO result = scheduleMapper.entityToResponseDTO(schedule1);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        assertEquals(LocalDate.of(2024, 11, 26), result.getScheduleStartDate());
        assertEquals(LocalDate.of(2024, 11, 26), result.getScheduleEndDate());
        assertEquals("Begin Excavation", result.getScheduleDescription());
        assertEquals("Lot 53", result.getLotId());
    }

    @Test
    void entityToResponseDTO_shouldHandleAllScheduleFields() {
        Schedule completeSchedule = Schedule.builder()
                .id(3)
                .scheduleIdentifier("SCH-003")
                .scheduleStartDate(LocalDate.of(2024, 12, 1))
                .scheduleEndDate(LocalDate.of(2024, 12, 1))
                .scheduleDescription("Electrical Work")
                .lotId(LOT_100_UUID)
                .tasks(new ArrayList<>())
                .build();

        ScheduleResponseDTO result = scheduleMapper.entityToResponseDTO(completeSchedule);

        assertNotNull(result);
        assertEquals("SCH-003", result.getScheduleIdentifier());
        assertEquals(LocalDate.of(2024, 12, 1), result.getScheduleStartDate());
        assertEquals(LocalDate.of(2024, 12, 1), result.getScheduleEndDate());
        assertEquals("Electrical Work", result.getScheduleDescription());
        assertEquals("Lot 100", result.getLotId());
    }

    @Test
    void entitiesToResponseDTOs_shouldMapMultipleSchedules() {
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);

        List<ScheduleResponseDTO> result = scheduleMapper.entitiesToResponseDTOs(schedules);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("SCH-001", result.get(0).getScheduleIdentifier());
        assertEquals("Begin Excavation", result.get(0).getScheduleDescription());
        assertEquals(LOT_53_UUID.toString(), result.get(0).getLotId());

        assertEquals("SCH-002", result.get(1).getScheduleIdentifier());
        assertEquals("Plumbing", result.get(1).getScheduleDescription());
        assertEquals(LOT_57_UUID.toString(), result.get(1).getLotId());
    }

    @Test
    void entitiesToResponseDTOs_shouldReturnEmptyListForEmptyInput() {
        List<Schedule> schedules = Collections.emptyList();

        List<ScheduleResponseDTO> result = scheduleMapper.entitiesToResponseDTOs(schedules);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void entitiesToResponseDTOs_shouldHandleSingleSchedule() {
        List<Schedule> schedules = Collections.singletonList(schedule1);

        List<ScheduleResponseDTO> result = scheduleMapper.entitiesToResponseDTOs(schedules);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SCH-001", result.get(0).getScheduleIdentifier());
    }
}
