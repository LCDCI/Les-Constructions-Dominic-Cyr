package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.ScheduleMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.MapperLayer.TaskMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleMapperWithProjectTest {

    private ScheduleMapper scheduleMapper;
    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();
        scheduleMapper = new ScheduleMapper(taskMapper);
    }

    @Test
    void entityToResponseDTO_shouldMapProjectFields() {
        // Arrange
        Project project = new Project();
        project.setProjectId(1L);
        project.setProjectIdentifier("proj-001-test");
        project.setProjectName("Test Project");

        Schedule schedule = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.of(2024, 11, 26))
                .scheduleEndDate(LocalDate.of(2024, 11, 26))
                .scheduleDescription("Begin Excavation")
                .lotId("Lot 53")
                .tasks(new ArrayList<>())
                .project(project)
                .build();

        // Act
        ScheduleResponseDTO result = scheduleMapper.entityToResponseDTO(schedule);

        // Assert
        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        assertEquals(LocalDate.of(2024, 11, 26), result.getScheduleStartDate());
        assertEquals(LocalDate.of(2024, 11, 26), result.getScheduleEndDate());
        assertEquals("Begin Excavation", result.getScheduleDescription());
        assertEquals("Lot 53", result.getLotId());
        
        // Verify project fields
        assertEquals(1L, result.getProjectId());
        assertEquals("proj-001-test", result.getProjectIdentifier());
        assertEquals("Test Project", result.getProjectName());
    }

    @Test
    void entityToResponseDTO_shouldHandleNullProject() {
        // Arrange
        Schedule schedule = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.of(2024, 11, 26))
                .scheduleEndDate(LocalDate.of(2024, 11, 26))
                .scheduleDescription("Begin Excavation")
                .lotId("Lot 53")
                .tasks(new ArrayList<>())
                .project(null)
                .build();

        // Act
        ScheduleResponseDTO result = scheduleMapper.entityToResponseDTO(schedule);

        // Assert
        assertNotNull(result);
        assertEquals("SCH-001", result.getScheduleIdentifier());
        
        // Verify project fields are null
        assertNull(result.getProjectId());
        assertNull(result.getProjectIdentifier());
        assertNull(result.getProjectName());
    }
}
