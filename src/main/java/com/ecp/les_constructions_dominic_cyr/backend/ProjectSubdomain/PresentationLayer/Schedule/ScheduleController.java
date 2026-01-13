package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.BadRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // Owner endpoints
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

    @PostMapping("/owners/schedules")
    public ResponseEntity<?> createOwnerSchedule(@RequestBody ScheduleRequestDTO scheduleRequestDTO) {
        try {
            ScheduleResponseDTO schedule = scheduleService.addSchedule(scheduleRequestDTO);
            return new ResponseEntity<>(schedule, HttpStatus.CREATED);
        } catch (InvalidInputException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/owners/schedules/{scheduleIdentifier}")
    public ResponseEntity<?> updateOwnerSchedule(@PathVariable String scheduleIdentifier, 
                                                  @RequestBody ScheduleRequestDTO scheduleRequestDTO) {
        try {
            ScheduleResponseDTO schedule = scheduleService.updateSchedule(scheduleIdentifier, scheduleRequestDTO);
            return ResponseEntity.ok(schedule);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidInputException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/owners/schedules/{scheduleIdentifier}")
    public ResponseEntity<?> deleteOwnerSchedule(@PathVariable String scheduleIdentifier) {
        try {
            scheduleService.deleteSchedule(scheduleIdentifier);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get task summary for owners - can view all contractor tasks.
     */
    @GetMapping("/owners/tasks/summary")
    public ResponseEntity<?> getOwnerCurrentWeekTaskSummary(
            @RequestParam(required = false) String contractorId) {
        try {
            TaskResponseDTO taskSummary = scheduleService.getCurrentWeekTaskSummary(contractorId);
            return ResponseEntity.ok(taskSummary);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get task summary for owners with custom date range.
     */
    @GetMapping("/owners/tasks/summary/{scheduleId}")
    public ResponseEntity<?> getOwnerTaskSummaryForPeriod(
            @PathVariable String scheduleId,
            @RequestParam(required = false) String contractorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {
        try {
            TaskResponseDTO taskSummary = scheduleService.getTaskSummaryForContractor(
                    contractorId, scheduleId, periodStart, periodEnd);
            return ResponseEntity.ok(taskSummary);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Salesperson endpoints
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

    // Contractor endpoints
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

    /**
     * Get task summary for current week for the contractor.
     * Tasks are viewable by contractors and owners.
     */
    @GetMapping("/contractors/tasks/summary")
    public ResponseEntity<?> getContractorCurrentWeekTaskSummary(
            @RequestParam String contractorId) {
        try {
            TaskResponseDTO taskSummary = scheduleService.getCurrentWeekTaskSummary(contractorId);
            return ResponseEntity.ok(taskSummary);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get task summary for a specific schedule and period for the contractor.
     */
    @GetMapping("/contractors/tasks/summary/{scheduleId}")
    public ResponseEntity<?> getContractorTaskSummaryForPeriod(
            @PathVariable String scheduleId,
            @RequestParam String contractorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {
        try {
            TaskResponseDTO taskSummary = scheduleService.getTaskSummaryForContractor(
                    contractorId, scheduleId, periodStart, periodEnd);
            return ResponseEntity.ok(taskSummary);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Customer endpoints
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

    // Get schedules by project identifier - accessible by all authenticated users
    @GetMapping("/projects/{projectIdentifier}/schedules")
    public ResponseEntity<List<ScheduleResponseDTO>> getProjectSchedules(@PathVariable String projectIdentifier) {
        try {
            log.info("Fetching schedules for project identifier: {}", projectIdentifier);
            List<ScheduleResponseDTO> schedules = scheduleService.getSchedulesByProjectIdentifier(projectIdentifier);
            log.info("Successfully fetched {} schedules for project: {}", schedules.size(), projectIdentifier);
            return ResponseEntity.ok(schedules);
        } catch (Exception ex) {
            log.error("Error fetching schedules for project {}: {}", projectIdentifier, ex.getMessage(), ex);
            return new ResponseEntity(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get a specific schedule by project identifier and schedule identifier
    @GetMapping("/projects/{projectIdentifier}/schedules/{scheduleIdentifier}")
    public ResponseEntity<?> getProjectScheduleByIdentifier(
            @PathVariable String projectIdentifier,
            @PathVariable String scheduleIdentifier) {
        try {
            log.info("Fetching schedule {} for project {}", scheduleIdentifier, projectIdentifier);
            ScheduleResponseDTO schedule = scheduleService.getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier);
            return ResponseEntity.ok(schedule);
        } catch (NotFoundException ex) {
            log.error("Schedule not found: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            log.error("Error fetching schedule {} for project {}: {}", scheduleIdentifier, projectIdentifier, ex.getMessage(), ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Create a new schedule for a specific project
    @PostMapping("/projects/{projectIdentifier}/schedules")
    public ResponseEntity<?> createProjectSchedule(
            @PathVariable String projectIdentifier,
            @RequestBody ScheduleRequestDTO scheduleRequestDTO) {
        try {
            log.info("Creating schedule for project: {}", projectIdentifier);
            ScheduleResponseDTO schedule = scheduleService.addScheduleToProject(projectIdentifier, scheduleRequestDTO);
            return new ResponseEntity<>(schedule, HttpStatus.CREATED);
        } catch (NotFoundException ex) {
            log.error("Project not found: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidInputException ex) {
            log.error("Invalid input: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Error creating schedule for project {}: {}", projectIdentifier, ex.getMessage(), ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update a schedule for a specific project
    @PutMapping("/projects/{projectIdentifier}/schedules/{scheduleIdentifier}")
    public ResponseEntity<?> updateProjectSchedule(
            @PathVariable String projectIdentifier,
            @PathVariable String scheduleIdentifier,
            @RequestBody ScheduleRequestDTO scheduleRequestDTO) {
        try {
            log.info("Updating schedule {} for project {}", scheduleIdentifier, projectIdentifier);
            ScheduleResponseDTO schedule = scheduleService.updateScheduleForProject(projectIdentifier, scheduleIdentifier, scheduleRequestDTO);
            return ResponseEntity.ok(schedule);
        } catch (NotFoundException ex) {
            log.error("Not found: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidInputException ex) {
            log.error("Invalid input: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Error updating schedule {} for project {}: {}", scheduleIdentifier, projectIdentifier, ex.getMessage(), ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a schedule from a specific project
    @DeleteMapping("/projects/{projectIdentifier}/schedules/{scheduleIdentifier}")
    public ResponseEntity<?> deleteProjectSchedule(
            @PathVariable String projectIdentifier,
            @PathVariable String scheduleIdentifier) {
        try {
            log.info("Deleting schedule {} from project {}", scheduleIdentifier, projectIdentifier);
            scheduleService.deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException ex) {
            log.error("Not found: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            log.error("Error deleting schedule {} from project {}: {}", scheduleIdentifier, projectIdentifier, ex.getMessage(), ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}