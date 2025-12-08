package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleMapperUnitTest {

    private ScheduleMapper scheduleMapper;
    private Schedule schedule1;
    private Schedule schedule2;

    @BeforeEach
    void setUp() {
        scheduleMapper = new ScheduleMapper();

        schedule1 = Schedule.builder()
                .id(1L)
                .scheduleIdentifier("SCH-001")
                .taskDate(LocalDate.of(2024, 11, 26))
                .taskDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .dayOfWeek("Wednesday")
                .build();

        schedule2 = Schedule.builder()
                .id(2L)
                .scheduleIdentifier("SCH-002")
                .taskDate(LocalDate.of(2024, 11, 27))
                .taskDescription("Plumbing")
                .lotNumber("Lot 57")
                .dayOfWeek("Thursday")
                .build();
    }

    @Test
    void entityToResponseDTO_shouldMapAllFieldsCorrectly() {
        ScheduleResponseDTO result = scheduleMapper.entityToResponseDTO(schedule1);

        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        assertEquals(LocalDate.of(2024, 11, 26), result.getTaskDate());
        assertEquals("Begin Excavation", result.getTaskDescription());
        assertEquals("Lot 53", result.getLotNumber());
        assertEquals("Wednesday", result.getDayOfWeek());
    }

    @Test
    void entityToResponseDTO_shouldHandleAllScheduleFields() {
        Schedule completeSchedule = Schedule.builder()
                .id(3L)
                .scheduleIdentifier("SCH-003")
                .taskDate(LocalDate.of(2024, 12, 1))
                .taskDescription("Electrical Work")
                .lotNumber("Lot 100")
                .dayOfWeek("Friday")
                .build();

        ScheduleResponseDTO result = scheduleMapper.entityToResponseDTO(completeSchedule);

        assertNotNull(result);
        assertEquals("SCH-003", result.getScheduleIdentifier());
        assertEquals(LocalDate.of(2024, 12, 1), result.getTaskDate());
        assertEquals("Electrical Work", result.getTaskDescription());
        assertEquals("Lot 100", result.getLotNumber());
        assertEquals("Friday", result.getDayOfWeek());
    }

    @Test
    void entitiesToResponseDTOs_shouldMapMultipleSchedules() {
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);

        List<ScheduleResponseDTO> result = scheduleMapper.entitiesToResponseDTOs(schedules);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("SCH-001", result.get(0).getScheduleIdentifier());
        assertEquals("Begin Excavation", result.get(0).getTaskDescription());
        assertEquals("Lot 53", result.get(0).getLotNumber());

        assertEquals("SCH-002", result.get(1).getScheduleIdentifier());
        assertEquals("Plumbing", result.get(1).getTaskDescription());
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