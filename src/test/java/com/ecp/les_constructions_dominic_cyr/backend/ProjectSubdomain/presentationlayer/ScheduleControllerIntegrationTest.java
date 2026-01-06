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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersPostgresConfig.class)
class ScheduleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    private final SimpleGrantedAuthority OWNER_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Schedule schedule1 = Schedule.builder()
                .scheduleIdentifier("SCH-TEST-001")
                .taskDate(startOfWeek)
                .taskDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .build();

        Schedule schedule2 = Schedule.builder()
                .scheduleIdentifier("SCH-TEST-002")
                .taskDate(startOfWeek.plusDays(1))
                .taskDescription("Plumbing")
                .lotNumber("Lot 57")
                .dayOfWeek("Tuesday")
                .build();

        Schedule schedule3 = Schedule.builder()
                .scheduleIdentifier("SCH-TEST-003")
                .taskDate(startOfWeek.plusDays(10))
                .taskDescription("Future Task")
                .lotNumber("Lot 99")
                .dayOfWeek("Thursday")
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);
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
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void getOwnerAllSchedules_shouldReturnAllSchedules() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/all")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
                .andExpect(jsonPath("$.scheduleIdentifier", is("SCH-TEST-001")))
                .andExpect(jsonPath("$.taskDescription", is("Begin Excavation")));
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturn500WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/SCH-INVALID")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // Salesperson, Contractor, and Customer tests follow the same pattern...
    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnSchedulesForCurrentWeek() throws Exception {
        mockMvc.perform(get("/api/v1/salesperson/schedules")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }
}