package com.ecp.les_constructions_dominic_cyr.ProjectSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Schedule;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.ScheduleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class ScheduleRepositoryIntegrationTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Schedule schedule1;
    private Schedule schedule2;
    private Schedule schedule3;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        schedule1 = Schedule.builder()
                .scheduleIdentifier("SCH-REPO-001")
                .taskDate(startOfWeek)
                .taskDescription("Begin Excavation")
                .lotNumber("Lot 53")
                .dayOfWeek("Monday")
                .build();

        schedule2 = Schedule.builder()
                .scheduleIdentifier("SCH-REPO-002")
                .taskDate(startOfWeek.plusDays(2))
                .taskDescription("Plumbing")
                .lotNumber("Lot 57")
                .dayOfWeek("Wednesday")
                .build();

        schedule3 = Schedule.builder()
                .scheduleIdentifier("SCH-REPO-003")
                .taskDate(startOfWeek.plusDays(20))
                .taskDescription("Future Task")
                .lotNumber("Lot 99")
                .dayOfWeek("Sunday")
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
    void findByScheduleIdentifier_shouldReturnScheduleWhenExists() {
        Optional<Schedule> result = scheduleRepository.findByScheduleIdentifier("SCH-REPO-001");

        assertTrue(result.isPresent());
        assertEquals("SCH-REPO-001", result.get().getScheduleIdentifier());
        assertEquals("Begin Excavation", result.get().getTaskDescription());
        assertEquals("Lot 53", result.get().getLotNumber());
    }

    @Test
    void findByScheduleIdentifier_shouldReturnEmptyWhenNotExists() {
        Optional<Schedule> result = scheduleRepository.findByScheduleIdentifier("SCH-INVALID");

        assertFalse(result.isPresent());
    }

    @Test
    void findCurrentWeekSchedules_shouldReturnSchedulesInRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Schedule> result = scheduleRepository.findCurrentWeekSchedules(startOfWeek, endOfWeek);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getScheduleIdentifier().equals("SCH-REPO-001")));
        assertTrue(result.stream().anyMatch(s -> s.getScheduleIdentifier().equals("SCH-REPO-002")));
        assertFalse(result.stream().anyMatch(s -> s.getScheduleIdentifier().equals("SCH-REPO-003")));
    }

    @Test
    void findByTaskDateBetween_shouldReturnSchedulesInDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Schedule> result = scheduleRepository.findByTaskDateBetween(startOfWeek, endOfWeek);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findAll_shouldReturnAllSchedules() {
        List<Schedule> result = scheduleRepository.findAll();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void save_shouldPersistSchedule() {
        Schedule newSchedule = Schedule.builder()
                .scheduleIdentifier("SCH-REPO-NEW")
                .taskDate(LocalDate.now())
                .taskDescription("New Task")
                .lotNumber("Lot 100")
                .dayOfWeek("Friday")
                .build();

        Schedule saved = scheduleRepository.save(newSchedule);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("SCH-REPO-NEW", saved.getScheduleIdentifier());

        Optional<Schedule> found = scheduleRepository.findByScheduleIdentifier("SCH-REPO-NEW");
        assertTrue(found.isPresent());
    }

    @Test
    void delete_shouldRemoveScheduleOContinuer() {
        scheduleRepository.delete(schedule1);
        Optional<Schedule> found = scheduleRepository.findByScheduleIdentifier("SCH-REPO-001");
        assertFalse(found.isPresent());
        List<Schedule> all = scheduleRepository.findAll();
        assertEquals(2, all.size());
    }
}