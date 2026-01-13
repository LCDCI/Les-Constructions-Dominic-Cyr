package com.ecp.les_constructions_dominic_cyr.backend.utils;

import org. junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

import static org. junit.jupiter.api.Assertions.*;

public class HttpErrorInfoTest {

    @Test
    void constructor_WithValidParameters_CreatesInstance() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String path = "/api/v1/projects/123";
        String message = "Project not found";

        HttpErrorInfo errorInfo = new HttpErrorInfo(status, path, message);

        assertNotNull(errorInfo);
        assertEquals(status, errorInfo.getHttpStatus());
        assertEquals(path, errorInfo.getPath());
        assertEquals(message, errorInfo.getMessage());
        assertNotNull(errorInfo.getTimestamp());
    }

    @Test
    void constructor_SetsTimestampAutomatically() {
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(1);

        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.OK, "/test", "message");

        ZonedDateTime after = ZonedDateTime.now().plusSeconds(1);

        assertNotNull(errorInfo.getTimestamp());
        assertTrue(errorInfo.getTimestamp().isAfter(before) || errorInfo.getTimestamp().isEqual(before));
        assertTrue(errorInfo.getTimestamp().isBefore(after) || errorInfo.getTimestamp().isEqual(after));
    }

    @Test
    void constructor_WithNotFoundStatus_CreatesCorrectInstance() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/api/v1/resource", "Not found");

        assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
        assertEquals(404, errorInfo.getHttpStatus().value());
    }

    @Test
    void constructor_WithBadRequestStatus_CreatesCorrectInstance() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, "/api/v1/lots", "Invalid input data");

        assertEquals(HttpStatus.BAD_REQUEST, errorInfo.getHttpStatus());
        assertEquals(400, errorInfo.getHttpStatus().value());
    }

    @Test
    void constructor_WithInternalServerErrorStatus_CreatesCorrectInstance() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "/api/v1/projects", "Unexpected error");

        assertEquals(HttpStatus. INTERNAL_SERVER_ERROR, errorInfo. getHttpStatus());
        assertEquals(500, errorInfo. getHttpStatus().value());
    }

    @Test
    void constructor_WithUnprocessableEntityStatus_CreatesCorrectInstance() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/api/v1/lots/123", "Invalid UUID format");

        assertEquals(HttpStatus. UNPROCESSABLE_ENTITY, errorInfo. getHttpStatus());
        assertEquals(422, errorInfo.getHttpStatus().value());
    }

    @Test
    void constructor_WithNullMessage_HandlesGracefully() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/test", null);

        assertNotNull(errorInfo);
        assertNull(errorInfo.getMessage());
        assertNotNull(errorInfo.getHttpStatus());
        assertNotNull(errorInfo.getPath());
        assertNotNull(errorInfo.getTimestamp());
    }

    @Test
    void constructor_WithNullPath_HandlesGracefully() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, null, "Error message");

        assertNotNull(errorInfo);
        assertNull(errorInfo.getPath());
        assertNotNull(errorInfo.getMessage());
    }

    @Test
    void constructor_WithEmptyPath_HandlesGracefully() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "", "Error message");

        assertNotNull(errorInfo);
        assertEquals("", errorInfo.getPath());
    }

    @Test
    void constructor_WithEmptyMessage_HandlesGracefully() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/test", "");

        assertNotNull(errorInfo);
        assertEquals("", errorInfo.getMessage());
    }

    @Test
    void getTimestamp_ReturnsNonNullValue() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.OK, "/test", "message");

        assertNotNull(errorInfo.getTimestamp());
    }

    @Test
    void getHttpStatus_ReturnsCorrectValue() {
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus. FORBIDDEN, "/secure", "Access denied");

        assertEquals(HttpStatus.FORBIDDEN, errorInfo.getHttpStatus());
    }

    @Test
    void getPath_ReturnsCorrectValue() {
        String expectedPath = "/api/v1/resources/abc-123";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, expectedPath, "Not found");

        assertEquals(expectedPath, errorInfo.getPath());
    }

    @Test
    void getMessage_ReturnsCorrectValue() {
        String expectedMessage = "The requested resource was not found";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, "/test", expectedMessage);

        assertEquals(expectedMessage, errorInfo.getMessage());
    }

    @Test
    void constructor_WithLongPath_HandlesCorrectly() {
        String longPath = "/api/v1/very/long/path/that/goes/on/and/on/forever/resource/12345";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, longPath, "Not found");

        assertEquals(longPath, errorInfo.getPath());
    }

    @Test
    void constructor_WithLongMessage_HandlesCorrectly() {
        String longMessage = "This is a very long error message that contains a lot of detail about what went wrong " +
                "including specific information about the error and how to fix it. It might also include " +
                "technical details that could be helpful for debugging purposes.";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, "/test", longMessage);

        assertEquals(longMessage, errorInfo.getMessage());
    }

    @Test
    void constructor_WithSpecialCharactersInPath_HandlesCorrectly() {
        String specialPath = "/api/v1/resource? param=value&other=123#section";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, specialPath, "Not found");

        assertEquals(specialPath, errorInfo.getPath());
    }

    @Test
    void constructor_WithSpecialCharactersInMessage_HandlesCorrectly() {
        String specialMessage = "Error: Invalid input <script>alert('xss')</script> & special chars: éàü";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, "/test", specialMessage);

        assertEquals(specialMessage, errorInfo.getMessage());
    }

    @Test
    void multipleInstances_HaveDifferentTimestamps() throws InterruptedException {
        HttpErrorInfo errorInfo1 = new HttpErrorInfo(HttpStatus.OK, "/test1", "message1");
        Thread.sleep(10);
        HttpErrorInfo errorInfo2 = new HttpErrorInfo(HttpStatus.OK, "/test2", "message2");

        assertNotEquals(errorInfo1. getTimestamp(), errorInfo2.getTimestamp());
    }
}