package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.TaskIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskIdentifierTest {

    @Test
    void testTaskIdentifierDefaultConstructor() {
        TaskIdentifier identifier = new TaskIdentifier();
        
        assertNotNull(identifier);
        assertNotNull(identifier.getTaskId());
        assertFalse(identifier.getTaskId().isEmpty());
        // UUID format check
        assertTrue(identifier.getTaskId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testTaskIdentifierConstructorWithParameter() {
        String taskId = "TASK-001";
        TaskIdentifier identifier = new TaskIdentifier(taskId);
        
        assertNotNull(identifier);
        assertEquals(taskId, identifier.getTaskId());
    }

    @Test
    void testTaskIdentifierGetter() {
        String taskId = "TASK-002";
        TaskIdentifier identifier = new TaskIdentifier(taskId);
        
        assertEquals(taskId, identifier.getTaskId());
    }

    @Test
    void testTaskIdentifierToStringWithValue() {
        String taskId = "TASK-003";
        TaskIdentifier identifier = new TaskIdentifier(taskId);
        
        String toString = identifier.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("TaskIdentifier"));
        assertTrue(toString.contains("taskId="));
        assertTrue(toString.contains(taskId));
    }

    @Test
    void testTaskIdentifierToStringWithNullValue() {
        TaskIdentifier identifier = new TaskIdentifier(null);
        
        String toString = identifier.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("TaskIdentifier"));
        assertTrue(toString.contains("taskId=''"));
    }

    @Test
    void testTaskIdentifierGeneratesUniqueIds() {
        TaskIdentifier identifier1 = new TaskIdentifier();
        TaskIdentifier identifier2 = new TaskIdentifier();
        
        assertNotEquals(identifier1.getTaskId(), identifier2.getTaskId());
    }

    @Test
    void testTaskIdentifierWithEmptyString() {
        String taskId = "";
        TaskIdentifier identifier = new TaskIdentifier(taskId);
        
        assertEquals(taskId, identifier.getTaskId());
        assertTrue(identifier.getTaskId().isEmpty());
    }

    @Test
    void testTaskIdentifierWithLongString() {
        String taskId = "TASK-" + "B".repeat(100);
        TaskIdentifier identifier = new TaskIdentifier(taskId);
        
        assertEquals(taskId, identifier.getTaskId());
        assertEquals(105, identifier.getTaskId().length());
    }

    @Test
    void testTaskIdentifierWithSpecialCharacters() {
        String taskId = "TASK-001-@#$%^&*()";
        TaskIdentifier identifier = new TaskIdentifier(taskId);
        
        assertEquals(taskId, identifier.getTaskId());
    }

    @Test
    void testTaskIdentifierWithNumericString() {
        String taskId = "123456789";
        TaskIdentifier identifier = new TaskIdentifier(taskId);
        
        assertEquals(taskId, identifier.getTaskId());
    }
}
