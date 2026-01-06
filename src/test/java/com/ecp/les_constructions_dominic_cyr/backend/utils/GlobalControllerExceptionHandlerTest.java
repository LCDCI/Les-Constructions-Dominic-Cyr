package com.ecp.les_constructions_dominic_cyr.backend.utils;

import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InUseException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidProjectDataException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ProjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        ProjectNotFoundException ex = new ProjectNotFoundException("Not found");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleProjectNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleInvalidProjectDataException_ReturnsBadRequestResponse() {
        InvalidProjectDataException ex = new InvalidProjectDataException("Bad data");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidProjectDataException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleGenericException_ReturnsInternalServerErrorResponse() {
        Exception ex = new Exception("Error");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleNotFoundException_ReturnsHttpErrorInfo() {
        NotFoundException ex = new NotFoundException("Not found");
        HttpErrorInfo error = exceptionHandler.handleNotFoundException(webRequest, ex);
        assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
    }

    @Test
    void handleInUseException_ReturnsHttpErrorInfo() {
        InUseException ex = new InUseException("In use");
        HttpErrorInfo error = exceptionHandler.handleInUseException(webRequest, ex);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getHttpStatus());
    }

    @Test
    void handleInvalidInputException_ReturnsHttpErrorInfo() {
        InvalidInputException ex = new InvalidInputException("Invalid");
        HttpErrorInfo error = exceptionHandler.handleInvalidInputException(webRequest, ex);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getHttpStatus());
    }

    @Test
    void handleBadRequestExceptions_ReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad request");
        HttpErrorInfo error = exceptionHandler.handleBadRequestExceptions(webRequest, ex);
        assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
    }
}