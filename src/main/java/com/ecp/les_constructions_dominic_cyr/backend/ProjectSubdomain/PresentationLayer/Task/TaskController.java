package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Task;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Task.TaskService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.BadRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import jakarta.validation.Valid;
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
public class TaskController {

    private final TaskService taskService;

    // Owner endpoints - Full CRUD access
    @GetMapping("/owners/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getOwnerCurrentWeekTasks() {
        List<TaskResponseDTO> tasks = taskService.getCurrentWeekTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/owners/tasks/all")
    public ResponseEntity<List<TaskResponseDTO>> getOwnerAllTasks() {
        List<TaskResponseDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/owners/tasks/{taskIdentifier}")
    public ResponseEntity<TaskResponseDTO> getOwnerTaskByIdentifier(@PathVariable String taskIdentifier) {
        try {
            TaskResponseDTO task = taskService.getTaskByIdentifier(taskIdentifier);
            return ResponseEntity.ok(task);
        } catch (NotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/owners/tasks")
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        try {
            TaskResponseDTO task = taskService.createTask(taskRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/owners/tasks/{taskIdentifier}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable String taskIdentifier,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        try {
            TaskResponseDTO task = taskService.updateTask(taskIdentifier, taskRequestDTO);
            return ResponseEntity.ok(task);
        } catch (NotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/owners/tasks/{taskIdentifier}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskIdentifier) {
        try {
            taskService.deleteTask(taskIdentifier);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException ex) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    // Contractor endpoints - Read-only access for their assigned tasks
    @GetMapping("/contractors/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getContractorCurrentWeekTasks(
            @RequestParam(required = true) String contractorId) {
        List<TaskResponseDTO> tasks = taskService.getCurrentWeekTasksAssignedToContractor(contractorId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/contractors/tasks/all")
    public ResponseEntity<List<TaskResponseDTO>> getContractorAllTasks(
            @RequestParam(required = true) String contractorId) {
        List<TaskResponseDTO> tasks = taskService.getTasksAssignedToContractor(contractorId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/contractors/tasks/{taskIdentifier}")
    public ResponseEntity<TaskResponseDTO> getContractorTaskByIdentifier(@PathVariable String taskIdentifier) {
        try {
            TaskResponseDTO task = taskService.getTaskByIdentifier(taskIdentifier);
            return ResponseEntity.ok(task);
        } catch (NotFoundException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException | InvalidRequestException ex) {
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
