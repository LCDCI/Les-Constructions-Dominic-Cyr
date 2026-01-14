package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleEntityTest {

    private Schedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
    }

    @Test
    void testScheduleDefaultConstructor() {
        Schedule newSchedule = new Schedule();
        
        assertNotNull(newSchedule);
        assertNull(newSchedule.getId());
        assertNull(newSchedule.getScheduleIdentifier());
        assertNull(newSchedule.getScheduleStartDate());
        assertNull(newSchedule.getScheduleEndDate());
        assertNull(newSchedule.getScheduleDescription());
        assertNull(newSchedule.getLotId());
    }

    @Test
    void testScheduleConstructorWithParameters() {
        String identifier = "SCH-001";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        String description = "Test Schedule";
        String lotId = "Lot 100";

        Schedule schedule = new Schedule(identifier, startDate, endDate, description, lotId);

        assertNotNull(schedule);
        assertEquals(identifier, schedule.getScheduleIdentifier());
        assertEquals(startDate, schedule.getScheduleStartDate());
        assertEquals(endDate, schedule.getScheduleEndDate());
        assertEquals(description, schedule.getScheduleDescription());
        assertEquals(lotId, schedule.getLotId());
        assertNotNull(schedule.getCreatedAt());
        assertNotNull(schedule.getUpdatedAt());
        assertNotNull(schedule.getTasks());
        assertTrue(schedule.getTasks().isEmpty());
    }

    @Test
    void testScheduleBuilder() {
        Integer id = 1;
        String identifier = "SCH-002";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(10);
        String description = "Builder Schedule";
        String lotId = "Lot 50";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        Schedule schedule = Schedule.builder()
                .id(id)
                .scheduleIdentifier(identifier)
                .scheduleStartDate(startDate)
                .scheduleEndDate(endDate)
                .scheduleDescription(description)
                .lotId(lotId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        assertNotNull(schedule);
        assertEquals(id, schedule.getId());
        assertEquals(identifier, schedule.getScheduleIdentifier());
        assertEquals(startDate, schedule.getScheduleStartDate());
        assertEquals(endDate, schedule.getScheduleEndDate());
        assertEquals(description, schedule.getScheduleDescription());
        assertEquals(lotId, schedule.getLotId());
        assertEquals(createdAt, schedule.getCreatedAt());
        assertEquals(updatedAt, schedule.getUpdatedAt());
    }

    @Test
    void testScheduleGettersAndSetters() {
        Integer id = 1;
        String identifier = "SCH-003";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        String description = "Getter Setter Schedule";
        String lotId = "Lot 75";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        List<Task> tasks = new ArrayList<>();
        Project project = new Project();
        project.setProjectId(1L);
        project.setProjectIdentifier("proj-001");

        schedule.setId(id);
        schedule.setScheduleIdentifier(identifier);
        schedule.setScheduleStartDate(startDate);
        schedule.setScheduleEndDate(endDate);
        schedule.setScheduleDescription(description);
        schedule.setLotId(lotId);
        schedule.setCreatedAt(createdAt);
        schedule.setUpdatedAt(updatedAt);
        schedule.setTasks(tasks);
        schedule.setProject(project);

        assertEquals(id, schedule.getId());
        assertEquals(identifier, schedule.getScheduleIdentifier());
        assertEquals(startDate, schedule.getScheduleStartDate());
        assertEquals(endDate, schedule.getScheduleEndDate());
        assertEquals(description, schedule.getScheduleDescription());
        assertEquals(lotId, schedule.getLotId());
        assertEquals(createdAt, schedule.getCreatedAt());
        assertEquals(updatedAt, schedule.getUpdatedAt());
        assertEquals(tasks, schedule.getTasks());
        assertEquals(project, schedule.getProject());
    }

    @Test
    void testLegacyLotNumberAccessors() {
        String lotId = "Lot 123";
        
        schedule.setLotNumber(lotId);
        
        assertEquals(lotId, schedule.getLotNumber());
        assertEquals(lotId, schedule.getLotId());
    }

    @Test
    void testSetLotIdDirectly() {
        String lotId = "Lot 456";
        
        schedule.setLotId(lotId);
        
        assertEquals(lotId, schedule.getLotId());
        assertEquals(lotId, schedule.getLotNumber());
    }

    @Test

    @Test

    @Test

    @Test

        // onUpdate() is protected and called by JPA

        // createdAt should remain the same
        assertEquals(originalCreatedAt, schedule.getCreatedAt());
        
        // updatedAt should be updated
        assertNotNull(schedule.getUpdatedAt());
        assertTrue(schedule.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testScheduleWithProject() {
        Project project = new Project();
        project.setProjectId(1L);
        project.setProjectIdentifier("proj-001");
        project.setProjectName("Test Project");

        schedule.setProject(project);

        assertNotNull(schedule.getProject());
        assertEquals(project, schedule.getProject());
        assertEquals("proj-001", schedule.getProject().getProjectIdentifier());
    }

    @Test
    void testScheduleWithTasks() {
        Task task1 = Task.builder().id(1).taskTitle("Task 1").build();
        Task task2 = Task.builder().id(2).taskTitle("Task 2").build();
        
        List<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        schedule.setTasks(tasks);

        assertNotNull(schedule.getTasks());
        assertEquals(2, schedule.getTasks().size());
        assertEquals("Task 1", schedule.getTasks().get(0).getTaskTitle());
        assertEquals("Task 2", schedule.getTasks().get(1).getTaskTitle());
    }

    @Test
    void testScheduleEqualsAndHashCode() {
        Schedule schedule1 = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleDescription("Test")
                .build();

        Schedule schedule2 = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleDescription("Test")
                .build();

        assertEquals(schedule1, schedule2);
        assertEquals(schedule1.hashCode(), schedule2.hashCode());
    }

    @Test
    void testScheduleToString() {
        Schedule schedule = Schedule.builder()
                .id(1)
                .scheduleIdentifier("SCH-001")
                .scheduleDescription("Test Schedule")
                .lotId("Lot 100")
                .build();

        String toString = schedule.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Schedule"));
    }
}
