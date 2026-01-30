package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LivingEnvironment.LivingEnvironmentService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment.LivingEnvironmentContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LivingEnvironmentControllerUnitTest {
    @Mock
    private LivingEnvironmentService livingEnvironmentService;

    @InjectMocks
    private LivingEnvironmentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLivingEnvironment_Positive() {
        var responseModel = new LivingEnvironmentResponseModel();
        responseModel.setProjectIdentifier("proj-001-foresta");
        responseModel.setLanguage("en");
        responseModel.setHeaderTitle("Test Header");
        when(livingEnvironmentService.getLivingEnvironment("proj-001-foresta", "en")).thenReturn(responseModel);

        ResponseEntity<?> response = controller.getLivingEnvironment("proj-001-foresta", "en");
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void getLivingEnvironment_Negative_NotFound() {
        when(livingEnvironmentService.getLivingEnvironment("proj-404", "en")).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = controller.getLivingEnvironment("proj-404", "en");
        assertEquals(404, response.getStatusCodeValue());
    }
}
