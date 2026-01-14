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
        Project project = Project.builder().id(1L).projectIdentifier("proj-001").build();

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
    void testPrePersistCallback() {
        Schedule newSchedule = Schedule.builder()
                .scheduleIdentifier("SCH-004")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("PrePersist Test")
                .lotId("Lot 200")
                .build();

        // Simulate @PrePersist by calling the method directly
        newSchedule.onCreate();

        assertNotNull(newSchedule.getCreatedAt());
        assertNotNull(newSchedule.getUpdatedAt());
        assertNotNull(newSchedule.getTasks());
    }

    @Test
    void testPrePersistDoesNotOverwriteExistingTimestamps() {
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime existingUpdatedAt = LocalDateTime.now().minusHours(2);

        Schedule schedule = Schedule.builder()
                .scheduleIdentifier("SCH-005")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("PrePersist Test")
                .lotId("Lot 300")
                .createdAt(existingCreatedAt)
                .updatedAt(existingUpdatedAt)
                .build();

        schedule.onCreate();

        assertEquals(existingCreatedAt, schedule.getCreatedAt());
        assertEquals(existingUpdatedAt, schedule.getUpdatedAt());
    }

    @Test
    void testPrePersistInitializesTasksList() {
        Schedule newSchedule = Schedule.builder()
                .scheduleIdentifier("SCH-006")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("Tasks Test")
                .lotId("Lot 400")
                .build();

        newSchedule.onCreate();

        assertNotNull(newSchedule.getTasks());
        assertTrue(newSchedule.getTasks().isEmpty());
    }

    @Test
    void testPreUpdateCallback() {
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(5);
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(2);

        Schedule schedule = Schedule.builder()
                .scheduleIdentifier("SCH-007")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("PreUpdate Test")
                .lotId("Lot 500")
                .createdAt(originalCreatedAt)
                .updatedAt(originalUpdatedAt)
                .build();

        // Wait a brief moment to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        schedule.onUpdate();

        // createdAt should remain the same
        assertEquals(originalCreatedAt, schedule.getCreatedAt());
        
        // updatedAt should be updated
        assertNotNull(schedule.getUpdatedAt());
        assertTrue(schedule.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testScheduleWithProject() {
        Project project = Project.builder()
                .id(1L)
                .projectIdentifier("proj-001")
                .projectName("Test Project")
                .build();

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
