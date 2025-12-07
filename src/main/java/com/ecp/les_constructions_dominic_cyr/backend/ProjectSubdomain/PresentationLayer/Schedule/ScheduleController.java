package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owners")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getCurrentWeekSchedules() {
        log.info("REST request to get current week schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getAllSchedules() {
        log.info("REST request to get all schedules");
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        log.info("REST request to get schedule by identifier: {}", scheduleIdentifier);
        ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
        return ResponseEntity.ok(schedule);
    }
}