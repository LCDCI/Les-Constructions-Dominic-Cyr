package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Task.TaskRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        task1 = Task.builder()
                .taskIdentifier("TASK-REPO-001")
                .taskDate(startOfWeek)
                .taskDescription("Foundation Work")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .assignedTo("contractor-123")
                .build();

        task2 = Task.builder()
                .taskIdentifier("TASK-REPO-002")
                .taskDate(startOfWeek.plusDays(2))
                .taskDescription("Framing")
                .lotNumber("Lot 57")
                .dayOfWeek("Wednesday")
                .assignedTo("contractor-123")
                .build();

        task3 = Task.builder()
                .taskIdentifier("TASK-REPO-003")
                .taskDate(startOfWeek.plusDays(20))
                .taskDescription("Future Task")
                .lotNumber("Lot 99")
                .dayOfWeek("Sunday")
                .assignedTo("contractor-456")
                .build();

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
    }

    @Test
    void findByTaskIdentifier_shouldReturnTaskWhenExists() {
        Optional<Task> result = taskRepository.findByTaskIdentifier("TASK-REPO-001");

        assertTrue(result.isPresent());
        assertEquals("TASK-REPO-001", result.get().getTaskIdentifier());
        assertEquals("Foundation Work", result.get().getTaskDescription());
        assertEquals("Lot 53", result.get().getLotNumber());
        assertEquals("contractor-123", result.get().getAssignedTo());
    }

    @Test
    void findByTaskIdentifier_shouldReturnEmptyWhenNotExists() {
        Optional<Task> result = taskRepository.findByTaskIdentifier("TASK-INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void findCurrentWeekTasks_shouldReturnTasksInRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Task> result = taskRepository.findCurrentWeekTasks(startOfWeek, endOfWeek);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getTaskIdentifier().equals("TASK-REPO-001")));
        assertTrue(result.stream().anyMatch(t -> t.getTaskIdentifier().equals("TASK-REPO-002")));
        assertFalse(result.stream().anyMatch(t -> t.getTaskIdentifier().equals("TASK-REPO-003")));
    }

    @Test
    void findByTaskDateBetween_shouldReturnTasksInDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Task> result = taskRepository.findByTaskDateBetween(startOfWeek, endOfWeek);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findByAssignedTo_shouldReturnTasksForContractor() {
        List<Task> result = taskRepository.findByAssignedTo("contractor-123");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getAssignedTo().equals("contractor-123")));
    }

    @Test
    void findByAssignedTo_shouldReturnEmptyListWhenNoMatch() {
        List<Task> result = taskRepository.findByAssignedTo("contractor-999");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findCurrentWeekTasksByAssignedTo_shouldReturnTasksForContractorInRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Task> result = taskRepository.findCurrentWeekTasksByAssignedTo("contractor-123", startOfWeek, endOfWeek);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getAssignedTo().equals("contractor-123")));
    }

    @Test
    void findCurrentWeekTasksByAssignedTo_shouldReturnEmptyListWhenNoMatchInRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Task> result = taskRepository.findCurrentWeekTasksByAssignedTo("contractor-456", startOfWeek, endOfWeek);

        assertNotNull(result);
        assertTrue(result.isEmpty()); // contractor-456's task is in the future
    }

    @Test
    void findAll_shouldReturnAllTasks() {
        List<Task> result = taskRepository.findAll();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void save_shouldPersistTask() {
        Task newTask = Task.builder()
                .taskIdentifier("TASK-REPO-NEW")
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Friday")
                .assignedTo("contractor-789")
                .build();

        Task saved = taskRepository.save(newTask);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("TASK-REPO-NEW", saved.getTaskIdentifier());
        assertEquals("contractor-789", saved.getAssignedTo());

        Optional<Task> found = taskRepository.findByTaskIdentifier("TASK-REPO-NEW");
        assertTrue(found.isPresent());
    }

    @Test
    void save_shouldPersistTaskWithNullAssignedTo() {
        Task newTask = Task.builder()
                .taskIdentifier("TASK-REPO-UNASSIGNED")
                .taskDate(LocalDate.now())
                .taskDescription("Unassigned Task")
                .lotNumber("Lot 101")
                .dayOfWeek("Saturday")
                .assignedTo(null)
                .build();

        Task saved = taskRepository.save(newTask);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("TASK-REPO-UNASSIGNED", saved.getTaskIdentifier());
        assertNull(saved.getAssignedTo());
    }

    @Test
    void delete_shouldRemoveTask() {
        taskRepository.delete(task1);
        Optional<Task> found = taskRepository.findByTaskIdentifier("TASK-REPO-001");
        assertFalse(found.isPresent());
        List<Task> all = taskRepository.findAll();
        assertEquals(2, all.size());
    }
}
