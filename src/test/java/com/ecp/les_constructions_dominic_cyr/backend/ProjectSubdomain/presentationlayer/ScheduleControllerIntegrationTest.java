package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import org.junit.jupiter.api.AfterEach;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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

    private Schedule schedule1;
    private Schedule schedule2;
    private Schedule schedule3;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter;

    private final SimpleGrantedAuthority OWNER_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        schedule1 = Schedule.builder()
                .scheduleIdentifier("SCH-TEST-001")
                .taskDate(startOfWeek)
                .taskDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .build();

        schedule2 = Schedule.builder()
                .scheduleIdentifier("SCH-TEST-002")
                .taskDate(startOfWeek.plusDays(1))
                .taskDescription("Plumbing")
                .lotNumber("Lot 57")
                .dayOfWeek("Tuesday")
                .build();

        schedule3 = Schedule.builder()
                .scheduleIdentifier("SCH-TEST-003")
                .taskDate(startOfWeek.plusDays(10))
                .taskDescription("Future Task")
                .lotNumber("Lot 99")
                .dayOfWeek("Thursday")
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);

        org.springframework.security.oauth2.jwt.Jwt mockJwtToken = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "test-owner")
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(mockJwtToken);
    }

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
    }

    @Test
    void getOwnerCurrentWeekSchedules_shouldReturnSchedulesForCurrentWeek() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules")
                .with(jwt().authorities(OWNER_ROLE))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].scheduleIdentifier", notNullValue()))
                .andExpect(jsonPath("$[0].taskDate", notNullValue()))
                .andExpect(jsonPath("$[0].taskDescription", notNullValue()))
                .andExpect(jsonPath("$[0].lotNumber", notNullValue()))
                .andExpect(jsonPath("$[0].dayOfWeek", notNullValue()));
    }

    @Test
    void getOwnerAllSchedules_shouldReturnAllSchedules() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/all")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].scheduleIdentifier",
                        containsInAnyOrder("SCH-TEST-001", "SCH-TEST-002", "SCH-TEST-003")));
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnScheduleWhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/SCH-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scheduleIdentifier", is("SCH-TEST-001")))
                .andExpect(jsonPath("$.taskDescription", is("Begin Excavation")))
                .andExpect(jsonPath("$.lotNumber", is("Lot 53")))
                .andExpect(jsonPath("$.dayOfWeek", is("Monday")));
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnSchedulesForCurrentWeek() throws Exception {
        mockMvc.perform(get("/api/v1/salesperson/schedules")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].scheduleIdentifier", notNullValue()))
                .andExpect(jsonPath("$[0].taskDate", notNullValue()))
                .andExpect(jsonPath("$[0].taskDescription", notNullValue()))
                .andExpect(jsonPath("$[0].lotNumber", notNullValue()))
                .andExpect(jsonPath("$[0].dayOfWeek", notNullValue()));
    }

    @Test
    void getAllSalespersonSchedules_shouldReturnAllSchedules() throws Exception {
        mockMvc.perform(get("/api/v1/salesperson/schedules/all")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].scheduleIdentifier",
                        containsInAnyOrder("SCH-TEST-001", "SCH-TEST-002", "SCH-TEST-003")));
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnScheduleWhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/salesperson/schedules/SCH-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scheduleIdentifier", is("SCH-TEST-001")))
                .andExpect(jsonPath("$.taskDescription", is("Begin Excavation")))
                .andExpect(jsonPath("$.lotNumber", is("Lot 53")))
                .andExpect(jsonPath("$.dayOfWeek", is("Monday")));
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/salesperson/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getContractorCurrentWeekSchedules_shouldReturnSchedulesForCurrentWeek() throws Exception {
        mockMvc.perform(get("/api/v1/contractors/schedules")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].scheduleIdentifier", notNullValue()))
                .andExpect(jsonPath("$[0].taskDate", notNullValue()))
                .andExpect(jsonPath("$[0].taskDescription", notNullValue()))
                .andExpect(jsonPath("$[0].lotNumber", notNullValue()))
                .andExpect(jsonPath("$[0].dayOfWeek", notNullValue()));
    }

    @Test
    void getAllContractorSchedules_shouldReturnAllSchedules() throws Exception {
        mockMvc.perform(get("/api/v1/contractors/schedules/all")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].scheduleIdentifier",
                        containsInAnyOrder("SCH-TEST-001", "SCH-TEST-002", "SCH-TEST-003")));
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnScheduleWhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/contractors/schedules/SCH-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scheduleIdentifier", is("SCH-TEST-001")))
                .andExpect(jsonPath("$.taskDescription", is("Begin Excavation")))
                .andExpect(jsonPath("$.lotNumber", is("Lot 53")))
                .andExpect(jsonPath("$.dayOfWeek", is("Monday")));
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/contractors/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllCustomerSchedules_shouldReturnAllSchedules() throws Exception {
        mockMvc.perform(get("/api/v1/customers/schedules/all")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].scheduleIdentifier",
                        containsInAnyOrder("SCH-TEST-001", "SCH-TEST-002", "SCH-TEST-003")));
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnScheduleWhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/customers/schedules/SCH-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scheduleIdentifier", is("SCH-TEST-001")))
                .andExpect(jsonPath("$.taskDescription", is("Begin Excavation")))
                .andExpect(jsonPath("$.lotNumber", is("Lot 53")))
                .andExpect(jsonPath("$.dayOfWeek", is("Monday")));
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/customers/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

}