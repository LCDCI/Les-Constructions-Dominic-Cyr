package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Schedule.ScheduleService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleRequestDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.ScheduleResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Schedule.TaskResponseDTO;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.BadRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidRequestException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerUnitTest{

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private UserService userService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ScheduleController scheduleController;

    private ScheduleResponseDTO responseDTO1;
    private ScheduleResponseDTO responseDTO2;
    private UserResponseModel ownerUser;

    @BeforeEach
    void setUp() {
        responseDTO1 = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-001")
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Begin Excavation")
                .lotId("Lot 53")
                .build();

        responseDTO2 = ScheduleResponseDTO.builder()
                .scheduleIdentifier("SCH-002")
                .scheduleStartDate(LocalDate.now().plusDays(1))
                .scheduleEndDate(LocalDate.now().plusDays(1))
                .scheduleDescription("Plumbing")
                .lotId("Lot 57")
                .build();

        // Setup owner user for tests
        ownerUser = new UserResponseModel();
        ownerUser.setUserIdentifier("user-123");
        ownerUser.setUserRole(UserRole.OWNER);
        ownerUser.setFirstName("Test");
        ownerUser.setLastName("Owner");
        ownerUser.setPrimaryEmail("owner@test.com");
    }

    // Owner Endpoints Tests
    @Test
    void getOwnerCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("SCH-001", response.getBody().get(0).getScheduleIdentifier());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getOwnerCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllOwnerSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getAllOwnerSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getAllSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getOwnerAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());
        assertEquals("Begin Excavation", response.getBody().getScheduleDescription());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Schedule not found with identifier: SCH-999";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid schedule identifier format";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getOwnerScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID-FORMAT";
        String errorMessage = "Invalid request format";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getOwnerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    // Contractor Endpoints Tests
    @Test
    void getContractorCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getContractorCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getContractorCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getContractorCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllContractorSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getContractorAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Schedule not found";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Bad request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getContractorScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getContractorScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    // Salesperson Endpoints Tests
    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getSalespersonCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllSalespersonSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getSalespersonAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Not found";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Bad request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getSalespersonScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getSalespersonScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    // Customer Endpoints Tests
    @Test
    void getCustomerCurrentWeekSchedules_shouldReturnSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getCustomerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getCustomerCurrentWeekSchedules_shouldReturnEmptyListWithOkStatus() {
        when(scheduleService.getCurrentWeekSchedules()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getCustomerCurrentWeekSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(scheduleService).getCurrentWeekSchedules();
    }

    @Test
    void getAllCustomerSchedules_shouldReturnAllSchedulesWithOkStatus() {
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        when(scheduleService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<ScheduleResponseDTO>> response = scheduleController.getCustomerAllSchedules();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(scheduleService).getAllSchedules();
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String identifier = "SCH-001";
        when(scheduleService.getScheduleByIdentifier(identifier)).thenReturn(responseDTO1);

        ResponseEntity<ScheduleResponseDTO> response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SCH-001", response.getBody().getScheduleIdentifier());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String identifier = "SCH-999";
        String errorMessage = "Not found";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnBadRequestWhenBadRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Bad request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new BadRequestException(errorMessage));

        ResponseEntity response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    @Test
    void getCustomerScheduleByIdentifier_shouldReturnBadRequestWhenInvalidRequestExceptionThrown() {
        String identifier = "INVALID";
        String errorMessage = "Invalid request";
        when(scheduleService.getScheduleByIdentifier(identifier))
                .thenThrow(new InvalidRequestException(errorMessage));

        ResponseEntity response = scheduleController.getCustomerScheduleByIdentifier(identifier);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByIdentifier(identifier);
    }

    // Project Schedule CRUD Endpoints Tests
    @Test
    void getProjectSchedules_shouldReturnSchedulesWithOkStatus() {
        String projectIdentifier = "proj-001";
        List<ScheduleResponseDTO> schedules = Arrays.asList(responseDTO1, responseDTO2);
        
        when(jwt.getSubject()).thenReturn("auth0|123");
        when(userService.getUserByAuth0Id("auth0|123")).thenReturn(ownerUser);
        when(scheduleService.getSchedulesByProjectIdentifier(projectIdentifier)).thenReturn(schedules);

        ResponseEntity<?> response = scheduleController.getProjectSchedules(projectIdentifier, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<ScheduleResponseDTO> body = (List<ScheduleResponseDTO>) response.getBody();
        assertEquals(2, body.size());

        verify(scheduleService).getSchedulesByProjectIdentifier(projectIdentifier);
    }

    @Test
    void getProjectSchedules_shouldReturnEmptyListWithOkStatus() {
        String projectIdentifier = "proj-001";
        
        when(jwt.getSubject()).thenReturn("auth0|123");
        when(userService.getUserByAuth0Id("auth0|123")).thenReturn(ownerUser);
        when(scheduleService.getSchedulesByProjectIdentifier(projectIdentifier)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = scheduleController.getProjectSchedules(projectIdentifier, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<ScheduleResponseDTO> body = (List<ScheduleResponseDTO>) response.getBody();
        assertTrue(body.isEmpty());

        verify(scheduleService).getSchedulesByProjectIdentifier(projectIdentifier);
    }

    @Test
    void getProjectScheduleByIdentifier_shouldReturnScheduleWithOkStatus() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";
        when(scheduleService.getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier))
                .thenReturn(responseDTO1);

        ResponseEntity<?> response = scheduleController.getProjectScheduleByIdentifier(projectIdentifier, scheduleIdentifier);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO1, response.getBody());

        verify(scheduleService).getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier);
    }

    @Test
    void getProjectScheduleByIdentifier_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-999";
        String errorMessage = "Schedule not found";
        when(scheduleService.getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = scheduleController.getProjectScheduleByIdentifier(projectIdentifier, scheduleIdentifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier);
    }

    @Test
    void getProjectScheduleByIdentifier_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";
        String errorMessage = "Unexpected error";
        when(scheduleService.getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.getProjectScheduleByIdentifier(projectIdentifier, scheduleIdentifier);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getScheduleByProjectAndScheduleIdentifier(projectIdentifier, scheduleIdentifier);
    }

    @Test
    void getProjectSchedules_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String projectIdentifier = "proj-001";
        String errorMessage = "Database error";
        
        when(jwt.getSubject()).thenReturn("auth0|123");
        when(userService.getUserByAuth0Id("auth0|123")).thenReturn(ownerUser);
        when(scheduleService.getSchedulesByProjectIdentifier(projectIdentifier))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.getProjectSchedules(projectIdentifier, jwt);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getSchedulesByProjectIdentifier(projectIdentifier);
    }

    // Tests for Owner Schedule CRUD operations
    @Test
    void createOwnerSchedule_shouldReturnCreatedWhenSuccessful() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("New schedule")
                .lotId("Lot 100")
                .build();

        when(scheduleService.addSchedule(requestDTO)).thenReturn(responseDTO1);

        ResponseEntity<?> response = scheduleController.createOwnerSchedule(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO1, response.getBody());

        verify(scheduleService).addSchedule(requestDTO);
    }

    @Test
    void createOwnerSchedule_shouldReturnBadRequestWhenInvalidInputExceptionThrown() {
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(null)
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("New schedule")
                .lotId("Lot 100")
                .build();

        String errorMessage = "Invalid input";
        when(scheduleService.addSchedule(requestDTO)).thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = scheduleController.createOwnerSchedule(requestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).addSchedule(requestDTO);
    }

    @Test
    void updateOwnerSchedule_shouldReturnOkWhenSuccessful() {
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now().plusDays(1))
                .scheduleEndDate(LocalDate.now().plusDays(8))
                .scheduleDescription("Updated schedule")
                .lotId("Lot 101")
                .build();

        when(scheduleService.updateSchedule(scheduleIdentifier, requestDTO)).thenReturn(responseDTO1);

        ResponseEntity<?> response = scheduleController.updateOwnerSchedule(scheduleIdentifier, requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO1, response.getBody());

        verify(scheduleService).updateSchedule(scheduleIdentifier, requestDTO);
    }

    @Test
    void updateOwnerSchedule_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String scheduleIdentifier = "SCH-999";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(7))
                .scheduleDescription("Updated schedule")
                .lotId("Lot 100")
                .build();

        String errorMessage = "Schedule not found";
        when(scheduleService.updateSchedule(scheduleIdentifier, requestDTO))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = scheduleController.updateOwnerSchedule(scheduleIdentifier, requestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).updateSchedule(scheduleIdentifier, requestDTO);
    }

    @Test
    void updateOwnerSchedule_shouldReturnBadRequestWhenInvalidInputExceptionThrown() {
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(null)
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Updated schedule")
                .lotId("Lot 100")
                .build();

        String errorMessage = "Invalid input";
        when(scheduleService.updateSchedule(scheduleIdentifier, requestDTO))
                .thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = scheduleController.updateOwnerSchedule(scheduleIdentifier, requestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).updateSchedule(scheduleIdentifier, requestDTO);
    }

    @Test
    void deleteOwnerSchedule_shouldReturnNoContentWhenSuccessful() {
        String scheduleIdentifier = "SCH-001";

        doNothing().when(scheduleService).deleteSchedule(scheduleIdentifier);

        ResponseEntity<?> response = scheduleController.deleteOwnerSchedule(scheduleIdentifier);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(scheduleService).deleteSchedule(scheduleIdentifier);
    }

    @Test
    void deleteOwnerSchedule_shouldReturnNotFoundWhenNotFoundExceptionThrown() {
        String scheduleIdentifier = "SCH-999";
        String errorMessage = "Schedule not found";

        doThrow(new NotFoundException(errorMessage)).when(scheduleService).deleteSchedule(scheduleIdentifier);

        ResponseEntity<?> response = scheduleController.deleteOwnerSchedule(scheduleIdentifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).deleteSchedule(scheduleIdentifier);
    }

    // Tests for Task Summary endpoints
    @Test
    void getOwnerCurrentWeekTaskSummary_shouldReturnTaskSummaryWithOkStatus() {
        String contractorId = "contractor-123";
        TaskResponseDTO taskSummary = TaskResponseDTO.builder()
                .contractorId(contractorId)
                .totalTasks(10)
                .openTasksCount(5)
                .completedTasksCount(5)
                .generatedAt(LocalDateTime.now())
                .build();

        when(scheduleService.getCurrentWeekTaskSummary(contractorId)).thenReturn(taskSummary);

        ResponseEntity<?> response = scheduleController.getOwnerCurrentWeekTaskSummary(contractorId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskSummary, response.getBody());

        verify(scheduleService).getCurrentWeekTaskSummary(contractorId);
    }

    @Test
    void getOwnerCurrentWeekTaskSummary_shouldReturnTaskSummaryWithNullContractor() {
        TaskResponseDTO taskSummary = TaskResponseDTO.builder()
                .contractorId(null)
                .totalTasks(15)
                .openTasksCount(8)
                .completedTasksCount(7)
                .generatedAt(LocalDateTime.now())
                .build();

        when(scheduleService.getCurrentWeekTaskSummary(null)).thenReturn(taskSummary);

        ResponseEntity<?> response = scheduleController.getOwnerCurrentWeekTaskSummary(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskSummary, response.getBody());

        verify(scheduleService).getCurrentWeekTaskSummary(null);
    }

    @Test
    void getOwnerCurrentWeekTaskSummary_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String contractorId = "contractor-123";
        String errorMessage = "Database error";

        when(scheduleService.getCurrentWeekTaskSummary(contractorId))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.getOwnerCurrentWeekTaskSummary(contractorId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getCurrentWeekTaskSummary(contractorId);
    }

    @Test
    void getOwnerTaskSummaryForPeriod_shouldReturnTaskSummaryWithOkStatus() {
        String scheduleId = "SCH-001";
        String contractorId = "contractor-123";
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = LocalDate.now().plusDays(7);

        TaskResponseDTO taskSummary = TaskResponseDTO.builder()
                .scheduleId(scheduleId)
                .contractorId(contractorId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .totalTasks(5)
                .generatedAt(LocalDateTime.now())
                .build();

        when(scheduleService.getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd))
                .thenReturn(taskSummary);

        ResponseEntity<?> response = scheduleController.getOwnerTaskSummaryForPeriod(
                scheduleId, contractorId, periodStart, periodEnd);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskSummary, response.getBody());

        verify(scheduleService).getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd);
    }

    @Test
    void getOwnerTaskSummaryForPeriod_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String scheduleId = "SCH-001";
        String contractorId = "contractor-123";
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = LocalDate.now().plusDays(7);
        String errorMessage = "Error generating summary";

        when(scheduleService.getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.getOwnerTaskSummaryForPeriod(
                scheduleId, contractorId, periodStart, periodEnd);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd);
    }

    @Test
    void getContractorCurrentWeekTaskSummary_shouldReturnTaskSummaryWithOkStatus() {
        String contractorId = "contractor-456";
        TaskResponseDTO taskSummary = TaskResponseDTO.builder()
                .contractorId(contractorId)
                .totalTasks(8)
                .openTasksCount(6)
                .completedTasksCount(2)
                .generatedAt(LocalDateTime.now())
                .build();

        when(scheduleService.getCurrentWeekTaskSummary(contractorId)).thenReturn(taskSummary);

        ResponseEntity<?> response = scheduleController.getContractorCurrentWeekTaskSummary(contractorId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskSummary, response.getBody());

        verify(scheduleService).getCurrentWeekTaskSummary(contractorId);
    }

    @Test
    void getContractorCurrentWeekTaskSummary_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String contractorId = "contractor-456";
        String errorMessage = "Service unavailable";

        when(scheduleService.getCurrentWeekTaskSummary(contractorId))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.getContractorCurrentWeekTaskSummary(contractorId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getCurrentWeekTaskSummary(contractorId);
    }

    @Test
    void getContractorTaskSummaryForPeriod_shouldReturnTaskSummaryWithOkStatus() {
        String scheduleId = "SCH-002";
        String contractorId = "contractor-789";
        LocalDate periodStart = LocalDate.now().minusDays(7);
        LocalDate periodEnd = LocalDate.now();

        TaskResponseDTO taskSummary = TaskResponseDTO.builder()
                .scheduleId(scheduleId)
                .contractorId(contractorId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .totalTasks(12)
                .generatedAt(LocalDateTime.now())
                .build();

        when(scheduleService.getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd))
                .thenReturn(taskSummary);

        ResponseEntity<?> response = scheduleController.getContractorTaskSummaryForPeriod(
                scheduleId, contractorId, periodStart, periodEnd);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskSummary, response.getBody());

        verify(scheduleService).getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd);
    }

    @Test
    void getContractorTaskSummaryForPeriod_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String scheduleId = "SCH-002";
        String contractorId = "contractor-789";
        LocalDate periodStart = LocalDate.now().minusDays(7);
        LocalDate periodEnd = LocalDate.now();
        String errorMessage = "Failed to fetch summary";

        when(scheduleService.getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.getContractorTaskSummaryForPeriod(
                scheduleId, contractorId, periodStart, periodEnd);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).getTaskSummaryForContractor(contractorId, scheduleId, periodStart, periodEnd);
    }

    // Tests for Project Schedule CRUD operations
    @Test
    void createProjectSchedule_shouldReturnCreatedWhenSuccessful() {
        String projectIdentifier = "proj-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(10))
                .scheduleDescription("Project schedule")
                .lotId("Lot 50")
                .build();

        when(scheduleService.addScheduleToProject(projectIdentifier, requestDTO)).thenReturn(responseDTO1);

        ResponseEntity<?> response = scheduleController.createProjectSchedule(projectIdentifier, requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO1, response.getBody());

        verify(scheduleService).addScheduleToProject(projectIdentifier, requestDTO);
    }

    @Test
    void createProjectSchedule_shouldReturnNotFoundWhenProjectNotFound() {
        String projectIdentifier = "proj-999";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(10))
                .scheduleDescription("Project schedule")
                .lotId("Lot 50")
                .build();

        String errorMessage = "Project not found";
        when(scheduleService.addScheduleToProject(projectIdentifier, requestDTO))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = scheduleController.createProjectSchedule(projectIdentifier, requestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).addScheduleToProject(projectIdentifier, requestDTO);
    }

    @Test
    void createProjectSchedule_shouldReturnBadRequestWhenInvalidInput() {
        String projectIdentifier = "proj-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(null)
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Project schedule")
                .lotId("Lot 50")
                .build();

        String errorMessage = "Invalid input";
        when(scheduleService.addScheduleToProject(projectIdentifier, requestDTO))
                .thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = scheduleController.createProjectSchedule(projectIdentifier, requestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).addScheduleToProject(projectIdentifier, requestDTO);
    }

    @Test
    void createProjectSchedule_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String projectIdentifier = "proj-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(10))
                .scheduleDescription("Project schedule")
                .lotId("Lot 50")
                .build();

        String errorMessage = "Database error";
        when(scheduleService.addScheduleToProject(projectIdentifier, requestDTO))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.createProjectSchedule(projectIdentifier, requestDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).addScheduleToProject(projectIdentifier, requestDTO);
    }

    @Test
    void updateProjectSchedule_shouldReturnOkWhenSuccessful() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now().plusDays(2))
                .scheduleEndDate(LocalDate.now().plusDays(12))
                .scheduleDescription("Updated project schedule")
                .lotId("Lot 51")
                .build();

        when(scheduleService.updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO))
                .thenReturn(responseDTO1);

        ResponseEntity<?> response = scheduleController.updateProjectSchedule(
                projectIdentifier, scheduleIdentifier, requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO1, response.getBody());

        verify(scheduleService).updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO);
    }

    @Test
    void updateProjectSchedule_shouldReturnNotFoundWhenProjectNotFound() {
        String projectIdentifier = "proj-999";
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(10))
                .scheduleDescription("Updated schedule")
                .lotId("Lot 50")
                .build();

        String errorMessage = "Project not found";
        when(scheduleService.updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO))
                .thenThrow(new NotFoundException(errorMessage));

        ResponseEntity<?> response = scheduleController.updateProjectSchedule(
                projectIdentifier, scheduleIdentifier, requestDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO);
    }

    @Test
    void updateProjectSchedule_shouldReturnBadRequestWhenInvalidInput() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(null)
                .scheduleEndDate(LocalDate.now())
                .scheduleDescription("Updated schedule")
                .lotId("Lot 50")
                .build();

        String errorMessage = "Invalid input";
        when(scheduleService.updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO))
                .thenThrow(new InvalidInputException(errorMessage));

        ResponseEntity<?> response = scheduleController.updateProjectSchedule(
                projectIdentifier, scheduleIdentifier, requestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO);
    }

    @Test
    void updateProjectSchedule_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";
        ScheduleRequestDTO requestDTO = ScheduleRequestDTO.builder()
                .scheduleStartDate(LocalDate.now())
                .scheduleEndDate(LocalDate.now().plusDays(10))
                .scheduleDescription("Updated schedule")
                .lotId("Lot 50")
                .build();

        String errorMessage = "Unexpected error";
        when(scheduleService.updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = scheduleController.updateProjectSchedule(
                projectIdentifier, scheduleIdentifier, requestDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).updateScheduleForProject(projectIdentifier, scheduleIdentifier, requestDTO);
    }

    @Test
    void deleteProjectSchedule_shouldReturnNoContentWhenSuccessful() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";

        doNothing().when(scheduleService).deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);

        ResponseEntity<?> response = scheduleController.deleteProjectSchedule(projectIdentifier, scheduleIdentifier);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(scheduleService).deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);
    }

    @Test
    void deleteProjectSchedule_shouldReturnNotFoundWhenScheduleNotFound() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-999";
        String errorMessage = "Schedule not found";

        doThrow(new NotFoundException(errorMessage))
                .when(scheduleService).deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);

        ResponseEntity<?> response = scheduleController.deleteProjectSchedule(projectIdentifier, scheduleIdentifier);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);
    }

    @Test
    void deleteProjectSchedule_shouldReturnInternalServerErrorWhenExceptionThrown() {
        String projectIdentifier = "proj-001";
        String scheduleIdentifier = "SCH-001";
        String errorMessage = "Failed to delete";

        doThrow(new RuntimeException(errorMessage))
                .when(scheduleService).deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);

        ResponseEntity<?> response = scheduleController.deleteProjectSchedule(projectIdentifier, scheduleIdentifier);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());

        verify(scheduleService).deleteScheduleFromProject(projectIdentifier, scheduleIdentifier);
    }
}