package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LivingEnvironment.LivingEnvironmentService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LivingEnvironment.LivingEnvironmentContent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LivingEnvironmentController.class)
class LivingEnvironmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LivingEnvironmentService livingEnvironmentService;

    @Test
    void getLivingEnvironment_Positive() throws Exception {
        var responseModel = new LivingEnvironmentResponseModel();
        responseModel.setProjectIdentifier("proj-001-foresta");
        responseModel.setLanguage("en");
        responseModel.setHeaderTitle("Test Header");
        when(livingEnvironmentService.getLivingEnvironment("proj-001-foresta", "en")).thenReturn(responseModel);

        mockMvc.perform(get("/api/v1/projects/proj-001-foresta/living-environment?lang=en"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getLivingEnvironment_Negative_NotFound() throws Exception {
        when(livingEnvironmentService.getLivingEnvironment("proj-404", "en")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/v1/projects/proj-404/living-environment?lang=en"))
                .andExpect(status().is4xxClientError());
    }
}
