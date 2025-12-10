package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.BadRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/owners/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getOwnerAllSchedules() {
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/owners/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getOwnerScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        try {
            ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
            return ResponseEntity.ok(schedule);
        } catch (NotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/salesperson/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getSalespersonCurrentWeekSchedules() {
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/salesperson/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getSalespersonAllSchedules() {
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/salesperson/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getSalespersonScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        try {
            ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
            return ResponseEntity.ok(schedule);
        } catch (NotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/contractors/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getContractorCurrentWeekSchedules() {
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/contractors/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getContractorAllSchedules() {
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/contractors/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getContractorScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        try {
            ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
            return ResponseEntity.ok(schedule);
        } catch (NotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/customers/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getCustomerCurrentWeekSchedules() {
        List<ScheduleResponseDTO> schedules = scheduleService.getCurrentWeekSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/customers/schedules/all")
    public ResponseEntity<List<ScheduleResponseDTO>> getCustomerAllSchedules() {
        List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/customers/schedules/{scheduleIdentifier}")
    public ResponseEntity<ScheduleResponseDTO> getCustomerScheduleByIdentifier(@PathVariable String scheduleIdentifier) {
        try {
            ScheduleResponseDTO schedule = scheduleService.getScheduleByIdentifier(scheduleIdentifier);
            return ResponseEntity.ok(schedule);
        } catch (NotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}