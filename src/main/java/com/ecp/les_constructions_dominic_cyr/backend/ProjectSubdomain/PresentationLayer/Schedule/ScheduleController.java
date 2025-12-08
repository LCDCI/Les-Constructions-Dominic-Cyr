package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/owners/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getOwnerCurrentWeekSchedules() {
        log.info("REST request to get current week schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/owners/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getOwnerAllSchedules() {
        log.info("REST request to get all schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/owners/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getOwnerScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        log.info("REST request to get schedule by identifier: {}", scheduleIdentifier);
        ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/salesperson/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getSalespersonCurrentWeekSchedules() {
        log.info("REST request to get current week schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/salesperson/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getSalespersonAllSchedules() {
        log.info("REST request to get all schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/salesperson/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getSalespersonScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        log.info("REST request to get schedule by identifier: {}", scheduleIdentifier);
        ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/contractors/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getContractorCurrentWeekSchedules() {
        log.info("REST request to get current week schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/contractors/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getContractorAllSchedules() {
        log.info("REST request to get all schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/contractors/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getContractorScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        log.info("REST request to get schedule by identifier: {}", scheduleIdentifier);
        ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
        return ResponseEntity.ok(schedule);
    }
}