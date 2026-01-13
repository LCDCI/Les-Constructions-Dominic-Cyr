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

        // Using standard setters instead of builder to avoid "cannot resolve method builder"
        Schedule schedule1 = new Schedule();
        schedule1.setScheduleIdentifier("SCH-TEST-001");
        schedule1.setTaskDate(startOfWeek);
        schedule1.setTaskDescription("Begin Excavation");
        schedule1.setLotNumber("Lot 53");
        schedule1.setDayOfWeek("Monday");

        Schedule schedule2 = new Schedule();
        schedule2.setScheduleIdentifier("SCH-TEST-002");
        schedule2.setTaskDate(startOfWeek.plusDays(1));
        schedule2.setTaskDescription("Plumbing");
        schedule2.setLotNumber("Lot 57");
        schedule2.setDayOfWeek("Tuesday");

        Schedule schedule3 = new Schedule();
        schedule3.setScheduleIdentifier("SCH-TEST-003");
        schedule3.setTaskDate(startOfWeek.plusDays(10));
        schedule3.setTaskDescription("Future Task");
        schedule3.setLotNumber("Lot 99");
        schedule3.setDayOfWeek("Thursday");

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);
    }

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
    }

    //@Test
    void getOwnerAllSchedules_shouldReturnAllSchedules() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/all")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].scheduleIdentifier",
                        containsInAnyOrder("SCH-TEST-001", "SCH-TEST-002", "SCH-TEST-003")));
    }

    //@Test
    void getOwnerScheduleByIdentifier_shouldReturnScheduleWhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/owners/schedules/SCH-TEST-001")
                        .with(jwt().authorities(OWNER_ROLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleIdentifier", is("SCH-TEST-001")))
                .andExpect(jsonPath("$.scheduleDescription", is("Begin Excavation")));
    }
}