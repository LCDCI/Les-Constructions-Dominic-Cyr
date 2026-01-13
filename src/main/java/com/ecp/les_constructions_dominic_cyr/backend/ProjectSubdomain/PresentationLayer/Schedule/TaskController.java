package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.TaskService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Schedule.Task;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
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
public class TaskController {

    private final TaskService taskService;

    // Owner endpoints - full CRUD operations
    @GetMapping("/owners/tasks")
    public ResponseEntity<List<TaskDetailResponseDTO>> getOwnerAllTasks() {
        List<TaskDetailResponseDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/owners/tasks/{taskId}")
    public ResponseEntity<?> getOwnerTaskById(@PathVariable String taskId) {
        try {
            TaskDetailResponseDTO task = taskService.getTaskByIdentifier(taskId);
            return ResponseEntity.ok(task);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/owners/tasks")
    public ResponseEntity<?> createOwnerTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        try {
            TaskDetailResponseDTO task = taskService.createTask(taskRequestDTO);
            return new ResponseEntity<>(task, HttpStatus.CREATED);
        } catch (InvalidInputException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/owners/tasks/{taskId}")
    public ResponseEntity<?> updateOwnerTask(@PathVariable String taskId, 
                                              @RequestBody TaskRequestDTO taskRequestDTO) {
        try {
            TaskDetailResponseDTO task = taskService.updateTask(taskId, taskRequestDTO);
            return ResponseEntity.ok(task);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidInputException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/owners/tasks/{taskId}")
    public ResponseEntity<?> deleteOwnerTask(@PathVariable String taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Contractor endpoints - read-only access to assigned tasks
    @GetMapping("/contractors/tasks")
    public ResponseEntity<?> getContractorTasks(@RequestParam String contractorId) {
        try {
            List<TaskDetailResponseDTO> tasks = taskService.getTasksForContractor(contractorId);
            return ResponseEntity.ok(tasks);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidInputException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/contractors/tasks/{taskId}")
    public ResponseEntity<?> getContractorTaskById(@PathVariable String taskId) {
        try {
            TaskDetailResponseDTO task = taskService.getTaskByIdentifier(taskId);
            return ResponseEntity.ok(task);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/schedules/{scheduleIdentifier}/tasks")
    public List<Task> getTasksForSchedule(@PathVariable String scheduleIdentifier) {
        return taskService.getTasksForSchedule(scheduleIdentifier);
    }
}
