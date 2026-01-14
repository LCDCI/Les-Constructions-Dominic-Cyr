package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskPriority;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskEntityTest {

    private Task task;
    private Users assignedUser;

    @BeforeEach
    void setUp() {
        task = new Task();
        
        assignedUser = new Users();
        assignedUser.setUserRole(UserRole.CONTRACTOR);
    }

    @Test
    void testTaskDefaultConstructor() {
        Task newTask = new Task();
        
        assertNotNull(newTask);
        assertNull(newTask.getId());
        assertNull(newTask.getTaskIdentifier());
        assertNull(newTask.getTaskStatus());
        assertNull(newTask.getTaskTitle());
        assertNull(newTask.getPeriodStart());
        assertNull(newTask.getPeriodEnd());
        assertNull(newTask.getTaskDescription());
        assertNull(newTask.getTaskPriority());
        assertNull(newTask.getEstimatedHours());
        assertNull(newTask.getHoursSpent());
        assertNull(newTask.getTaskProgress());
        assertNull(newTask.getAssignedTo());
        assertNull(newTask.getScheduleId());
    }

    @Test
    void testTaskConstructorWithAllParameters() {
        TaskIdentifier taskIdentifier = new TaskIdentifier("TASK-001");
        TaskStatus taskStatus = TaskStatus.IN_PROGRESS;
        String taskTitle = "Foundation Work";
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = LocalDate.now().plusDays(7);
        String taskDescription = "Complete foundation work";
        TaskPriority taskPriority = TaskPriority.HIGH;
        Double estimatedHours = 40.0;
        Double hoursSpent = 20.0;
        Double taskProgress = 50.0;

        Task task = new Task(taskIdentifier, taskStatus, taskTitle, periodStart, periodEnd,
                taskDescription, taskPriority, estimatedHours, hoursSpent, taskProgress, assignedUser);

        assertNotNull(task);
        assertEquals(taskIdentifier, task.getTaskIdentifier());
        assertEquals(taskStatus, task.getTaskStatus());
        assertEquals(taskTitle, task.getTaskTitle());
        assertEquals(periodStart, task.getPeriodStart());
        assertEquals(periodEnd, task.getPeriodEnd());
        assertEquals(taskDescription, task.getTaskDescription());
        assertEquals(taskPriority, task.getTaskPriority());
        assertEquals(estimatedHours, task.getEstimatedHours());
        assertEquals(hoursSpent, task.getHoursSpent());
        assertEquals(taskProgress, task.getTaskProgress());
        assertEquals(assignedUser, task.getAssignedTo());
    }

    @Test
    void testTaskBuilder() {
        Integer id = 1;
        TaskIdentifier taskIdentifier = new TaskIdentifier("TASK-002");
        TaskStatus taskStatus = TaskStatus.TO_DO;
        String taskTitle = "Framing";
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = LocalDate.now().plusDays(14);
        String taskDescription = "Frame the building";
        TaskPriority taskPriority = TaskPriority.MEDIUM;
        Double estimatedHours = 60.0;
        Double hoursSpent = 0.0;
        Double taskProgress = 0.0;
        String scheduleId = "SCH-001";

        Task task = Task.builder()
                .id(id)
                .taskIdentifier(taskIdentifier)
                .taskStatus(taskStatus)
                .taskTitle(taskTitle)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .taskDescription(taskDescription)
                .taskPriority(taskPriority)
                .estimatedHours(estimatedHours)
                .hoursSpent(hoursSpent)
                .taskProgress(taskProgress)
                .assignedTo(assignedUser)
                .scheduleId(scheduleId)
                .build();

        assertNotNull(task);
        assertEquals(id, task.getId());
        assertEquals(taskIdentifier, task.getTaskIdentifier());
        assertEquals(taskStatus, task.getTaskStatus());
        assertEquals(taskTitle, task.getTaskTitle());
        assertEquals(periodStart, task.getPeriodStart());
        assertEquals(periodEnd, task.getPeriodEnd());
        assertEquals(taskDescription, task.getTaskDescription());
        assertEquals(taskPriority, task.getTaskPriority());
        assertEquals(estimatedHours, task.getEstimatedHours());
        assertEquals(hoursSpent, task.getHoursSpent());
        assertEquals(taskProgress, task.getTaskProgress());
        assertEquals(assignedUser, task.getAssignedTo());
        assertEquals(scheduleId, task.getScheduleId());
    }

    @Test
    void testTaskGettersAndSetters() {
        Integer id = 1;
        TaskIdentifier taskIdentifier = new TaskIdentifier("TASK-003");
        TaskStatus taskStatus = TaskStatus.COMPLETED;
        String taskTitle = "Electrical Work";
        LocalDate periodStart = LocalDate.now().minusDays(10);
        LocalDate periodEnd = LocalDate.now().minusDays(3);
        String taskDescription = "Install electrical systems";
        TaskPriority taskPriority = TaskPriority.VERY_HIGH;
        Double estimatedHours = 50.0;
        Double hoursSpent = 48.0;
        Double taskProgress = 100.0;
        String scheduleId = "SCH-002";

        task.setId(id);
        task.setTaskIdentifier(taskIdentifier);
        task.setTaskStatus(taskStatus);
        task.setTaskTitle(taskTitle);
        task.setPeriodStart(periodStart);
        task.setPeriodEnd(periodEnd);
        task.setTaskDescription(taskDescription);
        task.setTaskPriority(taskPriority);
        task.setEstimatedHours(estimatedHours);
        task.setHoursSpent(hoursSpent);
        task.setTaskProgress(taskProgress);
        task.setAssignedTo(assignedUser);
        task.setScheduleId(scheduleId);

        assertEquals(id, task.getId());
        assertEquals(taskIdentifier, task.getTaskIdentifier());
        assertEquals(taskStatus, task.getTaskStatus());
        assertEquals(taskTitle, task.getTaskTitle());
        assertEquals(periodStart, task.getPeriodStart());
        assertEquals(periodEnd, task.getPeriodEnd());
        assertEquals(taskDescription, task.getTaskDescription());
        assertEquals(taskPriority, task.getTaskPriority());
        assertEquals(estimatedHours, task.getEstimatedHours());
        assertEquals(hoursSpent, task.getHoursSpent());
        assertEquals(taskProgress, task.getTaskProgress());
        assertEquals(assignedUser, task.getAssignedTo());
        assertEquals(scheduleId, task.getScheduleId());
    }

    @Test
    void testTaskStatusEnum() {
        task.setTaskStatus(TaskStatus.TO_DO);
        assertEquals(TaskStatus.TO_DO, task.getTaskStatus());

        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getTaskStatus());

        task.setTaskStatus(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, task.getTaskStatus());

        task.setTaskStatus(TaskStatus.ON_HOLD);
        assertEquals(TaskStatus.ON_HOLD, task.getTaskStatus());
    }

    @Test
    void testTaskPriorityEnum() {
        task.setTaskPriority(TaskPriority.VERY_LOW);
        assertEquals(TaskPriority.VERY_LOW, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.LOW);
        assertEquals(TaskPriority.LOW, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.MEDIUM);
        assertEquals(TaskPriority.MEDIUM, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.HIGH);
        assertEquals(TaskPriority.HIGH, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.VERY_HIGH);
        assertEquals(TaskPriority.VERY_HIGH, task.getTaskPriority());
    }

    @Test
    void testTaskWithNullAssignedUser() {
        task.setTaskTitle("Unassigned Task");
        task.setAssignedTo(null);

        assertEquals("Unassigned Task", task.getTaskTitle());
        assertNull(task.getAssignedTo());
    }

    @Test
    void testTaskProgressTracking() {
        task.setEstimatedHours(100.0);
        task.setHoursSpent(25.0);
        task.setTaskProgress(25.0);

        assertEquals(100.0, task.getEstimatedHours());
        assertEquals(25.0, task.getHoursSpent());
        assertEquals(25.0, task.getTaskProgress());

        // Update progress
        task.setHoursSpent(50.0);
        task.setTaskProgress(50.0);

        assertEquals(50.0, task.getHoursSpent());
        assertEquals(50.0, task.getTaskProgress());
    }

    @Test
    void testTaskToStringWithAssignedUser() {
        TaskIdentifier taskIdentifier = new TaskIdentifier("TASK-004");
        
        task = Task.builder()
                .id(1)
                .taskIdentifier(taskIdentifier)
                .taskStatus(TaskStatus.IN_PROGRESS)
                .taskTitle("Test Task")
                .periodStart(LocalDate.now())
                .periodEnd(LocalDate.now().plusDays(7))
                .taskDescription("Test description")
                .taskPriority(TaskPriority.HIGH)
                .estimatedHours(40.0)
                .hoursSpent(20.0)
                .taskProgress(50.0)
                .assignedTo(assignedUser)
                .scheduleId("SCH-001")
                .build();

        String toString = task.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Task{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("taskIdentifier="));
        assertTrue(toString.contains("taskStatus=IN_PROGRESS"));
        assertTrue(toString.contains("taskTitle='Test Task'"));
        assertTrue(toString.contains("taskPriority=HIGH"));
        assertTrue(toString.contains("estimatedHours=40.0"));
        assertTrue(toString.contains("hoursSpent=20.0"));
        assertTrue(toString.contains("taskProgress=50.0"));
        assertTrue(toString.contains("assignedTo=[Users instance]"));
        assertTrue(toString.contains("scheduleId='SCH-001'"));
    }

    @Test
    void testTaskToStringWithNullAssignedUser() {
        TaskIdentifier taskIdentifier = new TaskIdentifier("TASK-005");
        
        task = Task.builder()
                .id(2)
                .taskIdentifier(taskIdentifier)
                .taskStatus(TaskStatus.TO_DO)
                .taskTitle("Unassigned Task")
                .periodStart(LocalDate.now())
                .periodEnd(LocalDate.now().plusDays(5))
                .taskDescription("No assignment yet")
                .taskPriority(TaskPriority.LOW)
                .estimatedHours(20.0)
                .hoursSpent(0.0)
                .taskProgress(0.0)
                .assignedTo(null)
                .scheduleId("SCH-002")
                .build();

        String toString = task.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Task{"));
        assertTrue(toString.contains("assignedTo=null"));
    }

    @Test
    void testTaskEqualsAndHashCode() {
        TaskIdentifier taskIdentifier = new TaskIdentifier("TASK-006");
        
        Task task1 = Task.builder()
                .id(1)
                .taskIdentifier(taskIdentifier)
                .taskTitle("Test Task")
                .taskStatus(TaskStatus.IN_PROGRESS)
                .build();

        Task task2 = Task.builder()
                .id(1)
                .taskIdentifier(taskIdentifier)
                .taskTitle("Test Task")
                .taskStatus(TaskStatus.IN_PROGRESS)
                .build();

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void testTaskWithDifferentStatuses() {
        task.setTaskTitle("Status Test");
        task.setTaskStatus(TaskStatus.TO_DO);
        assertEquals(TaskStatus.TO_DO, task.getTaskStatus());

        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getTaskStatus());

        task.setTaskStatus(TaskStatus.ON_HOLD);
        assertEquals(TaskStatus.ON_HOLD, task.getTaskStatus());

        task.setTaskStatus(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, task.getTaskStatus());
    }

    @Test
    void testTaskWithDifferentPriorities() {
        task.setTaskTitle("Priority Test");
        
        task.setTaskPriority(TaskPriority.VERY_LOW);
        assertEquals(TaskPriority.VERY_LOW, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.LOW);
        assertEquals(TaskPriority.LOW, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.MEDIUM);
        assertEquals(TaskPriority.MEDIUM, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.HIGH);
        assertEquals(TaskPriority.HIGH, task.getTaskPriority());

        task.setTaskPriority(TaskPriority.VERY_HIGH);
        assertEquals(TaskPriority.VERY_HIGH, task.getTaskPriority());
    }
}
