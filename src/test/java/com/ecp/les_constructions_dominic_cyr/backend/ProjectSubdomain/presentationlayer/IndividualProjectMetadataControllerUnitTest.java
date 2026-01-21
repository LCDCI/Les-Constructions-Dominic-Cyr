package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.IndividualProjectMetadataService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.IndividualProjectMetadataController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.IndividualProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndividualProjectMetadataController.class)
@AutoConfigureMockMvc(addFilters = false)
public class IndividualProjectMetadataControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IndividualProjectMetadataService projectMetadataService;

    private IndividualProjectResponseModel testResponseModel;

    @BeforeEach
    void setUp() {
        testResponseModel = IndividualProjectResponseModel.builder()
                .projectIdentifier("proj-001")
                .projectName("Test Project")
                .projectDescription("Test Description")
                .status(ProjectStatus.IN_PROGRESS)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .primaryColor("#FFFFFF")
                .tertiaryColor("#000000")
                .buyerColor("#FF0000")
                .buyerName("Test Buyer")
                .imageIdentifier("img-001")
                .location("Test Location")
                .progressPercentage(50)
                .build();
    }

}
