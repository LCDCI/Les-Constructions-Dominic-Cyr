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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleEntityTest {

    private Schedule schedule;
    private static final UUID TEST_LOT_UUID = UUID.randomUUID();
    private static final UUID LOT_100_UUID = UUID.randomUUID();
    private static final UUID LOT_50_UUID = UUID.randomUUID();
    private static final UUID LOT_75_UUID = UUID.randomUUID();
    private static final UUID LOT_123_UUID = UUID.randomUUID();
    private static final UUID LOT_456_UUID = UUID.randomUUID();

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
        UUID lotId = TEST_LOT_UUID;

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
        UUID lotId = LOT_50_UUID;
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
        UUID lotId = LOT_75_UUID;
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
        String lotIdString = LOT_123_UUID.toString();

        schedule.setLotNumber(lotIdString);

        assertEquals(lotIdString, schedule.getLotNumber());
        assertEquals(LOT_123_UUID, schedule.getLotId());
    }

    @Test
    void testSetLotIdDirectly() {
        UUID lotId = LOT_456_UUID;

        schedule.setLotId(lotId);
        
        assertEquals(lotId, schedule.getLotId());
        assertEquals(lotId.toString(), schedule.getLotNumber());
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
                .lotId(LOT_100_UUID)
                .build();

        String toString = schedule.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Schedule"));
    }
}
