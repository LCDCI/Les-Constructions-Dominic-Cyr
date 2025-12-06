package com.ecp. les_constructions_dominic_cyr.utils;

import com.ecp. les_constructions_dominic_cyr.backend.utils.GlobalControllerExceptionHandler;
import com.ecp. les_constructions_dominic_cyr.backend.utils. HttpErrorInfo;
import com.ecp. les_constructions_dominic_cyr.backend.utils. Exception.InUseException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr. backend.utils.Exception.InvalidProjectDataException;
import com. ecp.les_constructions_dominic_cyr.backend.utils.Exception. NotFoundException;
import com.ecp. les_constructions_dominic_cyr.backend.utils. Exception.ProjectNotFoundException;
import org.junit.jupiter.api. BeforeEach;
import org.junit.jupiter.api. Test;
import org.springframework.http.HttpStatus;
import org. springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request. ServletWebRequest;
import org.springframework. web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter. api.Assertions.*;

public class GlobalControllerExceptionHandlerTest {

    private GlobalControllerExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalControllerExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleProjectNotFoundException_ReturnsNotFoundResponse() {
        String errorMessage = "Project not found with identifier: proj-001";
        ProjectNotFoundException exception = new ProjectNotFoundException(errorMessage);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleProjectNotFoundException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus. NOT_FOUND, response.getStatusCode());
        assertNotNull(response. getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response. getBody().get("error"));
        assertEquals(errorMessage, response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleProjectNotFoundException_ContainsAllRequiredFields() {
        ProjectNotFoundException exception = new ProjectNotFoundException("Test message");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleProjectNotFoundException(exception);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body. containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
        assertEquals(4, body.size());
    }

    @Test
    void handleInvalidProjectDataException_ReturnsBadRequestResponse() {
        String errorMessage = "Project name cannot be empty";
        InvalidProjectDataException exception = new InvalidProjectDataException(errorMessage);

        ResponseEntity<Map<String, Object>> response = exceptionHandler. handleInvalidProjectDataException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response. getBody().get("error"));
        assertEquals(errorMessage, response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleInvalidProjectDataException_ContainsAllRequiredFields() {
        InvalidProjectDataException exception = new InvalidProjectDataException("Validation failed");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidProjectDataException(exception);

        Map<String, Object> body = response. getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
        assertEquals(4, body.size());
    }

    @Test
    void handleGenericException_ReturnsInternalServerErrorResponse() {
        String errorMessage = "Something went wrong";
        Exception exception = new Exception(errorMessage);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response. getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody(). get("error"));
        assertEquals(errorMessage, response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleGenericException_ContainsAllRequiredFields() {
        Exception exception = new Exception("Generic error");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body. containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
        assertEquals(4, body.size());
    }

    @Test
    void handleGenericException_WithRuntimeException() {
        RuntimeException exception = new RuntimeException("Runtime error");

        ResponseEntity<Map<String, Object>> response = exceptionHandler. handleGenericException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Runtime error", response.getBody().get("message"));
    }

    @Test
    void handleGenericException_WithNullPointerException() {
        NullPointerException exception = new NullPointerException("Null reference");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus. INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleNotFoundException_ReturnsHttpErrorInfo() {
        String errorMessage = "Resource not found";
        NotFoundException exception = new NotFoundException(errorMessage);

        HttpErrorInfo errorInfo = exceptionHandler.handleNotFoundException(webRequest, exception);

        assertNotNull(errorInfo);
        assertEquals(HttpStatus. NOT_FOUND, errorInfo.getHttpStatus());
        assertEquals(errorMessage, errorInfo.getMessage());
        assertNotNull(errorInfo. getPath());
        assertNotNull(errorInfo. getTimestamp());
    }

    @Test
    void handleNotFoundException_VerifyPathIsIncluded() {
        NotFoundException exception = new NotFoundException("Not found");

        HttpErrorInfo errorInfo = exceptionHandler.handleNotFoundException(webRequest, exception);

        assertNotNull(errorInfo. getPath());
        assertFalse(errorInfo.getPath().isEmpty());
    }

    @Test
    void handleInUseException_ReturnsHttpErrorInfo() {
        String errorMessage = "Resource is currently in use";
        InUseException exception = new InUseException(errorMessage);

        HttpErrorInfo errorInfo = exceptionHandler.handleInUseException(webRequest, exception);

        assertNotNull(errorInfo);
        assertEquals(HttpStatus. UNPROCESSABLE_ENTITY, errorInfo.getHttpStatus());
        assertEquals(errorMessage, errorInfo.getMessage());
        assertNotNull(errorInfo.getPath());
        assertNotNull(errorInfo.getTimestamp());
    }

    @Test
    void handleInUseException_VerifyAllFieldsPopulated() {
        InUseException exception = new InUseException("Resource in use");

        HttpErrorInfo errorInfo = exceptionHandler.handleInUseException(webRequest, exception);

        assertNotNull(errorInfo. getTimestamp());
        assertNotNull(errorInfo.getPath());
        assertNotNull(errorInfo.getHttpStatus());
        assertNotNull(errorInfo.getMessage());
    }

    @Test
    void handleInvalidInputException_ReturnsHttpErrorInfo() {
        String errorMessage = "Invalid input provided";
        InvalidInputException exception = new InvalidInputException(errorMessage);

        HttpErrorInfo errorInfo = exceptionHandler.handleInvalidInputException(webRequest, exception);

        assertNotNull(errorInfo);
        assertEquals(HttpStatus. UNPROCESSABLE_ENTITY, errorInfo. getHttpStatus());
        assertEquals(errorMessage, errorInfo.getMessage());
        assertNotNull(errorInfo. getPath());
        assertNotNull(errorInfo.getTimestamp());
    }

    @Test
    void handleInvalidInputException_VerifyUnprocessableEntityStatus() {
        InvalidInputException exception = new InvalidInputException("Bad input");

        HttpErrorInfo errorInfo = exceptionHandler.handleInvalidInputException(webRequest, exception);

        assertEquals(HttpStatus. UNPROCESSABLE_ENTITY, errorInfo. getHttpStatus());
        assertEquals(422, errorInfo.getHttpStatus().value());
    }

    @Test
    void handleProjectNotFoundException_WithNullMessage() {
        ProjectNotFoundException exception = new ProjectNotFoundException(null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleProjectNotFoundException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus. NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody().get("message"));
    }

    @Test
    void handleInvalidProjectDataException_WithNullMessage() {
        InvalidProjectDataException exception = new InvalidProjectDataException(null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidProjectDataException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response. getStatusCode());
        assertNull(response.getBody().get("message"));
    }

    @Test
    void handleGenericException_WithNullMessage() {
        Exception exception = new Exception((String) null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody().get("message"));
    }

    @Test
    void handleNotFoundException_WithNullMessage() {
        NotFoundException exception = new NotFoundException((String) null);

        HttpErrorInfo errorInfo = exceptionHandler.handleNotFoundException(webRequest, exception);

        assertNotNull(errorInfo);
        assertNull(errorInfo. getMessage());
    }

    @Test
    void handleInUseException_WithNullMessage() {
        InUseException exception = new InUseException((String) null);

        HttpErrorInfo errorInfo = exceptionHandler.handleInUseException(webRequest, exception);

        assertNotNull(errorInfo);
        assertNull(errorInfo.getMessage());
    }

    @Test
    void handleInvalidInputException_WithNullMessage() {
        InvalidInputException exception = new InvalidInputException((String) null);

        HttpErrorInfo errorInfo = exceptionHandler.handleInvalidInputException(webRequest, exception);

        assertNotNull(errorInfo);
        assertNull(errorInfo.getMessage());
    }

    @Test
    void handleProjectNotFoundException_WithEmptyMessage() {
        ProjectNotFoundException exception = new ProjectNotFoundException("");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleProjectNotFoundException(exception);

        assertNotNull(response);
        assertEquals("", response.getBody(). get("message"));
    }

    @Test
    void handleInvalidProjectDataException_WithEmptyMessage() {
        InvalidProjectDataException exception = new InvalidProjectDataException("");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidProjectDataException(exception);

        assertNotNull(response);
        assertEquals("", response.getBody(). get("message"));
    }

    @Test
    void handleGenericException_WithEmptyMessage() {
        Exception exception = new Exception("");

        ResponseEntity<Map<String, Object>> response = exceptionHandler. handleGenericException(exception);

        assertNotNull(response);
        assertEquals("", response.getBody(). get("message"));
    }
}