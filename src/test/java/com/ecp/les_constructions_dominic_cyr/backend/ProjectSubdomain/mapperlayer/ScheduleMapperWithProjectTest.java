package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.mapperlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)

class ScheduleMapperWithProjectTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private LotRepository lotRepository;

    private ScheduleMapper scheduleMapper;
    private TaskMapper taskMapper;
    private static final UUID LOT_53_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper(scheduleRepository, lotRepository);
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
                .lotId(LOT_53_UUID)
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
        assertEquals(LOT_53_UUID.toString(), result.getLotId());

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
                .lotId(LOT_53_UUID)
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
