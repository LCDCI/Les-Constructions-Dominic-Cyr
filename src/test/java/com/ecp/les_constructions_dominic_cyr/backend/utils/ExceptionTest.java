package com.ecp.les_constructions_dominic_cyr.backend.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionTest {

    @Test
    void inUseException_DefaultConstructor_CreatesInstance() {
        InUseException exception = new InUseException();

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void inUseException_WithMessage_ReturnsCorrectMessage() {
        String message = "Resource is currently in use";
        InUseException exception = new InUseException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception. getCause());
    }

    @Test
    void inUseException_WithCause_ReturnsCause() {
        Throwable cause = new RuntimeException("Original cause");
        InUseException exception = new InUseException(cause);

        assertEquals(cause, exception. getCause());
        assertTrue(exception.getMessage().contains("Original cause"));
    }

    @Test
    void inUseException_WithMessageAndCause_ReturnsBoth() {
        String message = "Resource in use";
        Throwable cause = new RuntimeException("Original cause");
        InUseException exception = new InUseException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void inUseException_ThrowsCorrectly() {
        assertThrows(InUseException.class, () -> {
            throw new InUseException("Test exception");
        });
    }


    @Test
    void invalidInputException_DefaultConstructor_CreatesInstance() {
        InvalidInputException exception = new InvalidInputException();

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void invalidInputException_WithMessage_ReturnsCorrectMessage() {
        String message = "Invalid input provided";
        InvalidInputException exception = new InvalidInputException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void invalidInputException_WithCause_ReturnsCause() {
        Throwable cause = new IllegalArgumentException("Bad argument");
        InvalidInputException exception = new InvalidInputException(cause);

        assertEquals(cause, exception.getCause());
    }

    @Test
    void invalidInputException_WithMessageAndCause_ReturnsBoth() {
        String message = "Invalid input";
        Throwable cause = new IllegalArgumentException("Bad argument");
        InvalidInputException exception = new InvalidInputException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void invalidInputException_ThrowsCorrectly() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException("Test exception");
        });
    }



    @Test
    void invalidProjectDataException_WithMessage_ReturnsCorrectMessage() {
        String message = "Invalid project data";
        InvalidProjectDataException exception = new InvalidProjectDataException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void invalidProjectDataException_ThrowsCorrectly() {
        assertThrows(InvalidProjectDataException.class, () -> {
            throw new InvalidProjectDataException("Test exception");
        });
    }


    @Test
    void invalidProjectDataException_WithNullMessage() {
        InvalidProjectDataException exception = new InvalidProjectDataException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void invalidProjectDataException_WithEmptyMessage() {
        InvalidProjectDataException exception = new InvalidProjectDataException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void notFoundException_DefaultConstructor_CreatesInstance() {
        NotFoundException exception = new NotFoundException();

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void notFoundException_WithMessage_ReturnsCorrectMessage() {
        String message = "Resource not found";
        NotFoundException exception = new NotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void notFoundException_WithCause_ReturnsCause() {
        Throwable cause = new RuntimeException("Original cause");
        NotFoundException exception = new NotFoundException(cause);

        assertEquals(cause, exception. getCause());
    }

    @Test
    void notFoundException_WithMessageAndCause_ReturnsBoth() {
        String message = "Not found";
        Throwable cause = new RuntimeException("Original cause");
        NotFoundException exception = new NotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void notFoundException_ThrowsCorrectly() {
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("Test exception");
        });
    }



    @Test
    void projectNotFoundException_WithMessage_ReturnsCorrectMessage() {
        String message = "Project not found with identifier: proj-001";
        ProjectNotFoundException exception = new ProjectNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void projectNotFoundException_ThrowsCorrectly() {
        assertThrows(ProjectNotFoundException.class, () -> {
            throw new ProjectNotFoundException("Test exception");
        });
    }

    @Test
    void projectNotFoundException_InheritanceCheck() {
        ProjectNotFoundException exception = new ProjectNotFoundException("Test");

        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void projectNotFoundException_WithNullMessage() {
        ProjectNotFoundException exception = new ProjectNotFoundException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void projectNotFoundException_WithEmptyMessage() {
        ProjectNotFoundException exception = new ProjectNotFoundException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void invalidInputException_WithNullMessage() {
        InvalidInputException exception = new InvalidInputException((String) null);

        assertNull(exception.getMessage());
    }

    @Test
    void invalidInputException_WithEmptyMessage() {
        InvalidInputException exception = new InvalidInputException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void notFoundException_WithNullMessage() {
        NotFoundException exception = new NotFoundException((String) null);

        assertNull(exception. getMessage());
    }

    @Test
    void notFoundException_WithEmptyMessage() {
        NotFoundException exception = new NotFoundException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void inUseException_WithNullMessage() {
        InUseException exception = new InUseException((String) null);

        assertNull(exception. getMessage());
    }

    @Test
    void inUseException_WithEmptyMessage() {
        InUseException exception = new InUseException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void inUseException_WithNullCause() {
        InUseException exception = new InUseException((Throwable) null);

        assertNull(exception.getCause());
    }

    @Test
    void invalidInputException_WithNullCause() {
        InvalidInputException exception = new InvalidInputException((Throwable) null);

        assertNull(exception. getCause());
    }

    @Test
    void notFoundException_WithNullCause() {
        NotFoundException exception = new NotFoundException((Throwable) null);

        assertNull(exception.getCause());
    }

    @Test
    void inUseException_WithNullMessageAndNullCause() {
        InUseException exception = new InUseException(null, null);

        assertNull(exception. getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void invalidInputException_WithNullMessageAndNullCause() {
        InvalidInputException exception = new InvalidInputException(null, null);

        assertNull(exception. getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void notFoundException_WithNullMessageAndNullCause() {
        NotFoundException exception = new NotFoundException(null, null);

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exception_CanBeCaughtAsRuntimeException() {
        try {
            throw new InUseException("Test");
        } catch (RuntimeException e) {
            assertTrue(e instanceof InUseException);
        }
    }

    @Test
    void exception_ChainedCauses() {
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);
        InUseException exception = new InUseException("Top level", intermediateCause);

        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause(). getCause());
    }

    @Test
    void allExceptions_CanBeThrown() {
        assertDoesNotThrow(() -> {
            try {
                throw new InUseException("test");
            } catch (InUseException e) {
            }
        });

        assertDoesNotThrow(() -> {
            try {
                throw new InvalidInputException("test");
            } catch (InvalidInputException e) {
            }
        });

        assertDoesNotThrow(() -> {
            try {
                throw new InvalidProjectDataException("test");
            } catch (InvalidProjectDataException e) {
            }
        });

        assertDoesNotThrow(() -> {
            try {
                throw new NotFoundException("test");
            } catch (NotFoundException e) {
            }
        });

        assertDoesNotThrow(() -> {
            try {
                throw new ProjectNotFoundException("test");
            } catch (ProjectNotFoundException e) {
            }
        });
    }

    @Test
    void badRequestExceptionShouldStoreMessage() {
        String expectedMessage = "Bad Request occurred";
        BadRequestException exception = new BadRequestException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void invalidRequestExceptionShouldStoreMessage() {
        String expectedMessage = "Invalid Request details";
        InvalidRequestException exception = new InvalidRequestException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void forbiddenAccessExceptionShouldStoreMessage() {
        String expectedMessage = "Access is forbidden";
        ForbiddenAccessException exception = new ForbiddenAccessException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }
}