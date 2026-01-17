package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.FileServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.ProjectRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FileServiceClient fileServiceClient;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final SimpleGrantedAuthority ADMIN_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");
    private final String BASE_URI = "/api/v1/projects";

    @BeforeEach
    void setUp() {
        lotRepository.deleteAll();
        lotRepository.save(new Lot(new LotIdentifier("lot-001"), "Loc", 100f, "1000", "92.9", LotStatus.AVAILABLE));
        when(fileServiceClient.validateFileExists(anyString())).thenReturn(Mono.just(true));
    }

    @Test
    void whenValidCreate_thenReturnCreated() throws Exception {
        ProjectRequestModel req = new ProjectRequestModel();
        req.setProjectName("Integration Project");
        req.setProjectDescription("Integration Desc");
        req.setStatus(ProjectStatus.IN_PROGRESS);
        req.setStartDate(LocalDate.of(2025, 1, 1));
        req.setEndDate(LocalDate.of(2025, 12, 31));
        req.setPrimaryColor("#FFFFFF");
        req.setTertiaryColor("#000000");
        req.setBuyerColor("#FF0000");
        req.setImageIdentifier("image-integration-001");
        req.setBuyerName("Buyer Name");
        req.setCustomerId("cust-001");
        req.setLotIdentifiers(List.of("lot-001"));
        req.setProgressPercentage(10);

        mockMvc.perform(post(BASE_URI)
                        .with(jwt().authorities(ADMIN_ROLE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectName").value("Integration Project"));
    }
}