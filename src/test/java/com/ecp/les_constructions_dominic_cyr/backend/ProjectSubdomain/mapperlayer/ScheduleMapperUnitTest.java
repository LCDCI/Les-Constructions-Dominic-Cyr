package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleMapperUnitTest {

    private ScheduleMapper scheduleMapper;
    private TaskMapper taskMapper;
    private Schedule schedule1;
    private Schedule schedule2;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();
        scheduleMapper = new ScheduleMapper(taskMapper);

        schedule1 = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.of(2024, 11, 26))
                .scheduleEndDate(LocalDate.of(2024, 11, 26))
                .scheduleDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .tasks(new ArrayList<>())
                .build();

        schedule2 = Schedule.builder()
                .id(2)
                .scheduleIdentifier("SCH-002")
                .scheduleStartDate(LocalDate.of(2024, 11, 27))
                .scheduleEndDate(LocalDate.of(2024, 11, 27))
                .scheduleDescription("Plumbing")
                .lotNumber("Lot 57")
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
        assertEquals("Lot 53", result.getLotNumber());
    }

    @Test
    void entityToResponseDTO_shouldHandleAllScheduleFields() {
        Schedule completeSchedule = Schedule.builder()
                .id(3)
                .scheduleIdentifier("SCH-003")
                .scheduleStartDate(LocalDate.of(2024, 12, 1))
                .scheduleEndDate(LocalDate.of(2024, 12, 1))
                .scheduleDescription("Electrical Work")
                .lotNumber("Lot 100")
                .tasks(new ArrayList<>())
                .build();

        ScheduleResponseDTO result = scheduleMapper.entityToResponseDTO(completeSchedule);

        assertNotNull(result);
        assertEquals("SCH-003", result.getScheduleIdentifier());
        assertEquals(LocalDate.of(2024, 12, 1), result.getScheduleStartDate());
        assertEquals(LocalDate.of(2024, 12, 1), result.getScheduleEndDate());
        assertEquals("Electrical Work", result.getScheduleDescription());
        assertEquals("Lot 100", result.getLotNumber());
    }

    @Test
    void entitiesToResponseDTOs_shouldMapMultipleSchedules() {
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);

        List<ScheduleResponseDTO> result = scheduleMapper.entitiesToResponseDTOs(schedules);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("SCH-001", result.get(0).getScheduleIdentifier());
        assertEquals("Begin Excavation", result.get(0).getScheduleDescription());
        assertEquals("Lot 53", result.get(0).getLotNumber());

        assertEquals("SCH-002", result.get(1).getScheduleIdentifier());
        assertEquals("Plumbing", result.get(1).getScheduleDescription());
        assertEquals("Lot 57", result.get(1).getLotNumber());
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