package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleIdentifierTest {

    @Test
    void testScheduleIdentifierDefaultConstructor() {
        ScheduleIdentifier identifier = new ScheduleIdentifier();
        
        assertNotNull(identifier);
        assertNotNull(identifier.getScheduleId());
        assertFalse(identifier.getScheduleId().isEmpty());
        // UUID format check
        assertTrue(identifier.getScheduleId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testScheduleIdentifierConstructorWithParameter() {
        String scheduleId = "SCH-001";
        ScheduleIdentifier identifier = new ScheduleIdentifier(scheduleId);
        
        assertNotNull(identifier);
        assertEquals(scheduleId, identifier.getScheduleId());
    }

    @Test
    void testScheduleIdentifierGetter() {
        String scheduleId = "SCH-002";
        ScheduleIdentifier identifier = new ScheduleIdentifier(scheduleId);
        
        assertEquals(scheduleId, identifier.getScheduleId());
    }

    @Test
    void testScheduleIdentifierToStringWithValue() {
        String scheduleId = "SCH-003";
        ScheduleIdentifier identifier = new ScheduleIdentifier(scheduleId);
        
        String toString = identifier.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ScheduleIdentifier"));
        assertTrue(toString.contains("scheduleId="));
        assertTrue(toString.contains(scheduleId));
    }

    @Test
    void testScheduleIdentifierToStringWithNullValue() {
        ScheduleIdentifier identifier = new ScheduleIdentifier(null);
        
        String toString = identifier.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ScheduleIdentifier"));
        assertTrue(toString.contains("scheduleId=''"));
    }

    @Test
    void testScheduleIdentifierGeneratesUniqueIds() {
        ScheduleIdentifier identifier1 = new ScheduleIdentifier();
        ScheduleIdentifier identifier2 = new ScheduleIdentifier();
        
        assertNotEquals(identifier1.getScheduleId(), identifier2.getScheduleId());
    }

    @Test
    void testScheduleIdentifierWithEmptyString() {
        String scheduleId = "";
        ScheduleIdentifier identifier = new ScheduleIdentifier(scheduleId);
        
        assertEquals(scheduleId, identifier.getScheduleId());
        assertTrue(identifier.getScheduleId().isEmpty());
    }

    @Test
    void testScheduleIdentifierWithLongString() {
        String scheduleId = "SCH-" + "A".repeat(100);
        ScheduleIdentifier identifier = new ScheduleIdentifier(scheduleId);
        
        assertEquals(scheduleId, identifier.getScheduleId());
        assertEquals(104, identifier.getScheduleId().length());
    }

    @Test
    void testScheduleIdentifierWithSpecialCharacters() {
        String scheduleId = "SCH-001-@#$%";
        ScheduleIdentifier identifier = new ScheduleIdentifier(scheduleId);
        
        assertEquals(scheduleId, identifier.getScheduleId());
    }
}
