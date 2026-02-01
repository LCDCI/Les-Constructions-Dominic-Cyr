package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationCategoryTest {

    @Test
    void enum_ContainsAllExpectedValues() {
        // Act
        NotificationCategory[] values = NotificationCategory.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        // Verify specific expected values exist
        assertTrue(containsValue(values, NotificationCategory.TASK_ASSIGNED));
        assertTrue(containsValue(values, NotificationCategory.TASK_COMPLETED));
        assertTrue(containsValue(values, NotificationCategory.PROJECT_CREATED));
        assertTrue(containsValue(values, NotificationCategory.PROJECT_UPDATED));
        assertTrue(containsValue(values, NotificationCategory.INQUIRY_RECEIVED));
        assertTrue(containsValue(values, NotificationCategory.GENERAL));
    }

    @Test
    void valueOf_WithValidName_ReturnsCorrectEnum() {
        // Act & Assert
        assertEquals(NotificationCategory.TASK_ASSIGNED, NotificationCategory.valueOf("TASK_ASSIGNED"));
        assertEquals(NotificationCategory.PROJECT_CREATED, NotificationCategory.valueOf("PROJECT_CREATED"));
        assertEquals(NotificationCategory.GENERAL, NotificationCategory.valueOf("GENERAL"));
        assertEquals(NotificationCategory.INQUIRY_RECEIVED, NotificationCategory.valueOf("INQUIRY_RECEIVED"));
    }

    @Test
    void valueOf_WithInvalidName_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationCategory.valueOf("INVALID_CATEGORY");
        });
    }

    @Test
    void name_ReturnsCorrectString() {
        // Act & Assert
        assertEquals("TASK_ASSIGNED", NotificationCategory.TASK_ASSIGNED.name());
        assertEquals("PROJECT_CREATED", NotificationCategory.PROJECT_CREATED.name());
        assertEquals("GENERAL", NotificationCategory.GENERAL.name());
    }

    @Test
    void enum_AllValuesAreUnique() {
        // Arrange
        NotificationCategory[] values = NotificationCategory.values();

        // Act & Assert
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i], values[j], 
                    "Duplicate enum values found: " + values[i]);
            }
        }
    }

    @Test
    void enum_CanBeUsedInSwitchStatement() {
        // Arrange
        NotificationCategory category = NotificationCategory.TASK_ASSIGNED;
        String result;

        // Act
        switch (category) {
            case TASK_ASSIGNED:
            case TASK_COMPLETED:
            case TASK_UPDATED:
                result = "TASK";
                break;
            case PROJECT_CREATED:
            case PROJECT_UPDATED:
            case PROJECT_COMPLETED:
                result = "PROJECT";
                break;
            case GENERAL:
                result = "GENERAL";
                break;
            default:
                result = "OTHER";
        }

        // Assert
        assertEquals("TASK", result);
    }

    @Test
    void enum_ToString_ReturnsName() {
        // Act & Assert
        assertEquals("TASK_ASSIGNED", NotificationCategory.TASK_ASSIGNED.toString());
        assertEquals("PROJECT_CREATED", NotificationCategory.PROJECT_CREATED.toString());
        assertEquals("GENERAL", NotificationCategory.GENERAL.toString());
    }

    private boolean containsValue(NotificationCategory[] values, NotificationCategory target) {
        for (NotificationCategory value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
