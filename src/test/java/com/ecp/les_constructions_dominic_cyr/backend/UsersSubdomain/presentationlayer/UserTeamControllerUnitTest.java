package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UsersController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.backend.utils.GlobalControllerExceptionHandler.class)
public class UserTeamControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private UserResponseModel activeContractor;
    private UserResponseModel activeSalesperson;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        activeContractor = new UserResponseModel();
        activeContractor.setUserIdentifier(UUID.randomUUID().toString());
        activeContractor.setFirstName("John");
        activeContractor.setLastName("Contractor");
        activeContractor.setPrimaryEmail("john.contractor@example.com");
        activeContractor.setPhone("514-555-0001");
        activeContractor.setUserRole(UserRole.CONTRACTOR);
        activeContractor.setUserStatus(UserStatus.ACTIVE);

        activeSalesperson = new UserResponseModel();
        activeSalesperson.setUserIdentifier(UUID.randomUUID().toString());
        activeSalesperson.setFirstName("Jane");
        activeSalesperson.setLastName("Salesperson");
        activeSalesperson.setPrimaryEmail("jane.sales@example.com");
        activeSalesperson.setPhone("514-555-0002");
        activeSalesperson.setUserRole(UserRole.SALESPERSON);
        activeSalesperson.setUserStatus(UserStatus.ACTIVE);
    }

    // ========================== GET ACTIVE CONTRACTORS TESTS ==========================

    @Test
    @WithMockUser
    void getActiveContractors_WithContractors_ReturnsOk() throws Exception {
        List<UserResponseModel> contractors = Arrays.asList(activeContractor);
        when(userService.getActiveContractors()).thenReturn(contractors);

        mockMvc.perform(get("/api/v1/users/contractors/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Contractor"))
                .andExpect(jsonPath("$[0].userRole").value("CONTRACTOR"))
                .andExpect(jsonPath("$[0].userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$[0].primaryEmail").value("john.contractor@example.com"));

        verify(userService, times(1)).getActiveContractors();
    }

    @Test
    @WithMockUser
    void getActiveContractors_WithMultipleContractors_ReturnsAll() throws Exception {
        UserResponseModel secondContractor = new UserResponseModel();
        secondContractor.setUserIdentifier(UUID.randomUUID().toString());
        secondContractor.setFirstName("Mike");
        secondContractor.setLastName("Builder");
        secondContractor.setPrimaryEmail("mike.builder@example.com");
        secondContractor.setUserRole(UserRole.CONTRACTOR);
        secondContractor.setUserStatus(UserStatus.ACTIVE);

        List<UserResponseModel> contractors = Arrays.asList(activeContractor, secondContractor);
        when(userService.getActiveContractors()).thenReturn(contractors);

        mockMvc.perform(get("/api/v1/users/contractors/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Mike"));

        verify(userService, times(1)).getActiveContractors();
    }

    @Test
    @WithMockUser
    void getActiveContractors_WithNoContractors_ReturnsEmptyArray() throws Exception {
        when(userService.getActiveContractors()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/contractors/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).getActiveContractors();
    }

    @Test
    @WithMockUser
    void getActiveContractors_ServiceThrowsException_Returns500() throws Exception {
        when(userService.getActiveContractors())
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(get("/api/v1/users/contractors/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getActiveContractors();
    }

    @Test
    @WithMockUser
    void getActiveContractors_IncludesAllRequiredFields() throws Exception {
        UserResponseModel contractorWithAllFields = new UserResponseModel();
        contractorWithAllFields.setUserIdentifier(UUID.randomUUID().toString());
        contractorWithAllFields.setFirstName("Complete");
        contractorWithAllFields.setLastName("Contractor");
        contractorWithAllFields.setPrimaryEmail("complete@example.com");
        contractorWithAllFields.setSecondaryEmail("secondary@example.com");
        contractorWithAllFields.setPhone("514-555-1234");
        contractorWithAllFields.setUserRole(UserRole.CONTRACTOR);
        contractorWithAllFields.setUserStatus(UserStatus.ACTIVE);

        when(userService.getActiveContractors())
                .thenReturn(Arrays.asList(contractorWithAllFields));

        mockMvc.perform(get("/api/v1/users/contractors/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userIdentifier").exists())
                .andExpect(jsonPath("$[0].firstName").value("Complete"))
                .andExpect(jsonPath("$[0].lastName").value("Contractor"))
                .andExpect(jsonPath("$[0].primaryEmail").value("complete@example.com"))
                .andExpect(jsonPath("$[0].secondaryEmail").value("secondary@example.com"))
                .andExpect(jsonPath("$[0].phone").value("514-555-1234"))
                .andExpect(jsonPath("$[0].userRole").value("CONTRACTOR"))
                .andExpect(jsonPath("$[0].userStatus").value("ACTIVE"));

        verify(userService, times(1)).getActiveContractors();
    }

    // ========================== GET ACTIVE SALESPERSONS TESTS ==========================

    @Test
    @WithMockUser
    void getActiveSalespersons_WithSalespersons_ReturnsOk() throws Exception {
        List<UserResponseModel> salespersons = Arrays.asList(activeSalesperson);
        when(userService.getActiveSalespersons()).thenReturn(salespersons);

        mockMvc.perform(get("/api/v1/users/salespersons/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].lastName").value("Salesperson"))
                .andExpect(jsonPath("$[0].userRole").value("SALESPERSON"))
                .andExpect(jsonPath("$[0].userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$[0].primaryEmail").value("jane.sales@example.com"));

        verify(userService, times(1)).getActiveSalespersons();
    }

    @Test
    @WithMockUser
    void getActiveSalespersons_WithMultipleSalespersons_ReturnsAll() throws Exception {
        UserResponseModel secondSalesperson = new UserResponseModel();
        secondSalesperson.setUserIdentifier(UUID.randomUUID().toString());
        secondSalesperson.setFirstName("Tom");
        secondSalesperson.setLastName("Seller");
        secondSalesperson.setPrimaryEmail("tom.seller@example.com");
        secondSalesperson.setUserRole(UserRole.SALESPERSON);
        secondSalesperson.setUserStatus(UserStatus.ACTIVE);

        List<UserResponseModel> salespersons = Arrays.asList(activeSalesperson, secondSalesperson);
        when(userService.getActiveSalespersons()).thenReturn(salespersons);

        mockMvc.perform(get("/api/v1/users/salespersons/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].firstName").value("Tom"));

        verify(userService, times(1)).getActiveSalespersons();
    }

    @Test
    @WithMockUser
    void getActiveSalespersons_WithNoSalespersons_ReturnsEmptyArray() throws Exception {
        when(userService.getActiveSalespersons()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/salespersons/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).getActiveSalespersons();
    }

    @Test
    @WithMockUser
    void getActiveSalespersons_ServiceThrowsException_Returns500() throws Exception {
        when(userService.getActiveSalespersons())
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(get("/api/v1/users/salespersons/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getActiveSalespersons();
    }

    @Test
    @WithMockUser
    void getActiveSalespersons_IncludesAllRequiredFields() throws Exception {
        UserResponseModel salespersonWithAllFields = new UserResponseModel();
        salespersonWithAllFields.setUserIdentifier(UUID.randomUUID().toString());
        salespersonWithAllFields.setFirstName("Complete");
        salespersonWithAllFields.setLastName("Salesperson");
        salespersonWithAllFields.setPrimaryEmail("complete.sales@example.com");
        salespersonWithAllFields.setSecondaryEmail("secondary.sales@example.com");
        salespersonWithAllFields.setPhone("514-555-5678");
        salespersonWithAllFields.setUserRole(UserRole.SALESPERSON);
        salespersonWithAllFields.setUserStatus(UserStatus.ACTIVE);

        when(userService.getActiveSalespersons())
                .thenReturn(Arrays.asList(salespersonWithAllFields));

        mockMvc.perform(get("/api/v1/users/salespersons/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userIdentifier").exists())
                .andExpect(jsonPath("$[0].firstName").value("Complete"))
                .andExpect(jsonPath("$[0].lastName").value("Salesperson"))
                .andExpect(jsonPath("$[0].primaryEmail").value("complete.sales@example.com"))
                .andExpect(jsonPath("$[0].secondaryEmail").value("secondary.sales@example.com"))
                .andExpect(jsonPath("$[0].phone").value("514-555-5678"))
                .andExpect(jsonPath("$[0].userRole").value("SALESPERSON"))
                .andExpect(jsonPath("$[0].userStatus").value("ACTIVE"));

        verify(userService, times(1)).getActiveSalespersons();
    }

    // ========================== EDGE CASE TESTS ==========================

    @Test
    @WithMockUser
    void getActiveContractors_WithNullOptionalFields_ReturnsSuccessfully() throws Exception {
        UserResponseModel contractorWithNulls = new UserResponseModel();
        contractorWithNulls.setUserIdentifier(UUID.randomUUID().toString());
        contractorWithNulls.setFirstName("Minimal");
        contractorWithNulls.setLastName("Contractor");
        contractorWithNulls.setPrimaryEmail("minimal@example.com");
        contractorWithNulls.setSecondaryEmail(null);
        contractorWithNulls.setPhone(null);
        contractorWithNulls.setUserRole(UserRole.CONTRACTOR);
        contractorWithNulls.setUserStatus(UserStatus.ACTIVE);

        when(userService.getActiveContractors())
                .thenReturn(Arrays.asList(contractorWithNulls));

        mockMvc.perform(get("/api/v1/users/contractors/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Minimal"))
                .andExpect(jsonPath("$[0].primaryEmail").value("minimal@example.com"))
                .andExpect(jsonPath("$[0].secondaryEmail").doesNotExist())
                .andExpect(jsonPath("$[0].phone").doesNotExist());

        verify(userService, times(1)).getActiveContractors();
    }

    @Test
    @WithMockUser
    void getActiveSalespersons_WithNullOptionalFields_ReturnsSuccessfully() throws Exception {
        UserResponseModel salespersonWithNulls = new UserResponseModel();
        salespersonWithNulls.setUserIdentifier(UUID.randomUUID().toString());
        salespersonWithNulls.setFirstName("Minimal");
        salespersonWithNulls.setLastName("Salesperson");
        salespersonWithNulls.setPrimaryEmail("minimal.sales@example.com");
        salespersonWithNulls.setSecondaryEmail(null);
        salespersonWithNulls.setPhone(null);
        salespersonWithNulls.setUserRole(UserRole.SALESPERSON);
        salespersonWithNulls.setUserStatus(UserStatus.ACTIVE);

        when(userService.getActiveSalespersons())
                .thenReturn(Arrays.asList(salespersonWithNulls));

        mockMvc.perform(get("/api/v1/users/salespersons/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Minimal"))
                .andExpect(jsonPath("$[0].primaryEmail").value("minimal.sales@example.com"))
                .andExpect(jsonPath("$[0].secondaryEmail").doesNotExist())
                .andExpect(jsonPath("$[0].phone").doesNotExist());

        verify(userService, times(1)).getActiveSalespersons();
    }
}
