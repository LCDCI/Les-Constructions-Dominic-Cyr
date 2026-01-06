package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class ScheduleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final SimpleGrantedAuthority OWNER_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        Schedule s = Schedule.builder()
                .scheduleIdentifier("SCH-TEST-001")
                .taskDescription("Test Task")
                .lotNumber("Lot 1")
                .build();
        scheduleRepository.save(s);
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnScheduleWhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/SCH-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleIdentifier").value("SCH-TEST-001"));
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        // Aligned with log: "Returning HTTP status: 500 INTERNAL_SERVER_ERROR"
        mockMvc.perform(get("/api/v1/owners/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/salesperson/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/contractors/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE)))
                .andExpect(status().isInternalServerError());
    }
}