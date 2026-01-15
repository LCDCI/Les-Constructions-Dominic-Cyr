package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserTeamServiceUnitTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private Auth0ManagementService auth0ManagementService;

    @InjectMocks
    private UserServiceImpl userService;

    private Users activeContractor;
    private Users inactiveContractor;
    private Users deactivatedContractor;
    private Users activeSalesperson;
    private Users inactiveSalesperson;
    private Users activeOwner;
    private Users activeCustomer;

    @BeforeEach
    void setUp() {
        // Active Contractor
        activeContractor = new Users();
        activeContractor.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        activeContractor.setFirstName("John");
        activeContractor.setLastName("Contractor");
        activeContractor.setPrimaryEmail("john.contractor@example.com");
        activeContractor.setPhone("514-555-0001");
        activeContractor.setUserRole(UserRole.CONTRACTOR);
        activeContractor.setUserStatus(UserStatus.ACTIVE);

        // Inactive Contractor
        inactiveContractor = new Users();
        inactiveContractor.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        inactiveContractor.setFirstName("Bob");
        inactiveContractor.setLastName("Builder");
        inactiveContractor.setPrimaryEmail("bob.builder@example.com");
        inactiveContractor.setUserRole(UserRole.CONTRACTOR);
        inactiveContractor.setUserStatus(UserStatus.INACTIVE);

        // Deactivated Contractor
        deactivatedContractor = new Users();
        deactivatedContractor.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        deactivatedContractor.setFirstName("Removed");
        deactivatedContractor.setLastName("Contractor");
        deactivatedContractor.setPrimaryEmail("removed.contractor@example.com");
        deactivatedContractor.setUserRole(UserRole.CONTRACTOR);
        deactivatedContractor.setUserStatus(UserStatus.DEACTIVATED);

        // Active Salesperson
        activeSalesperson = new Users();
        activeSalesperson.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        activeSalesperson.setFirstName("Jane");
        activeSalesperson.setLastName("Salesperson");
        activeSalesperson.setPrimaryEmail("jane.sales@example.com");
        activeSalesperson.setPhone("514-555-0002");
        activeSalesperson.setUserRole(UserRole.SALESPERSON);
        activeSalesperson.setUserStatus(UserStatus.ACTIVE);

        // Inactive Salesperson
        inactiveSalesperson = new Users();
        inactiveSalesperson.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        inactiveSalesperson.setFirstName("Sarah");
        inactiveSalesperson.setLastName("Seller");
        inactiveSalesperson.setPrimaryEmail("sarah.seller@example.com");
        inactiveSalesperson.setUserRole(UserRole.SALESPERSON);
        inactiveSalesperson.setUserStatus(UserStatus.INACTIVE);

        // Active Owner (should not appear in contractor/salesperson lists)
        activeOwner = new Users();
        activeOwner.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        activeOwner.setFirstName("Owner");
        activeOwner.setLastName("User");
        activeOwner.setPrimaryEmail("owner@example.com");
        activeOwner.setUserRole(UserRole.OWNER);
        activeOwner.setUserStatus(UserStatus.ACTIVE);

        // Active Customer (should not appear in contractor/salesperson lists)
        activeCustomer = new Users();
        activeCustomer.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        activeCustomer.setFirstName("Customer");
        activeCustomer.setLastName("User");
        activeCustomer.setPrimaryEmail("customer@example.com");
        activeCustomer.setUserRole(UserRole.CUSTOMER);
        activeCustomer.setUserStatus(UserStatus.ACTIVE);
    }

    // ========================== GET ACTIVE CONTRACTORS TESTS ==========================

    @Test
    void getActiveContractors_WithActiveContractors_ReturnsOnlyActiveContractors() {
        List<Users> allActiveUsers = Arrays.asList(
                activeContractor,
                activeSalesperson,
                activeOwner,
                activeCustomer
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveContractors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Contractor", result.get(0).getLastName());
        assertEquals(UserRole.CONTRACTOR, result.get(0).getUserRole());
        assertEquals("john.contractor@example.com", result.get(0).getPrimaryEmail());

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveContractors_WithMultipleActiveContractors_ReturnsAll() {
        Users secondContractor = new Users();
        secondContractor.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        secondContractor.setFirstName("Mike");
        secondContractor.setLastName("Builder");
        secondContractor.setPrimaryEmail("mike.builder@example.com");
        secondContractor.setUserRole(UserRole.CONTRACTOR);
        secondContractor.setUserStatus(UserStatus.ACTIVE);

        List<Users> allActiveUsers = Arrays.asList(
                activeContractor,
                secondContractor,
                activeSalesperson
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveContractors();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.getUserRole() == UserRole.CONTRACTOR));
        assertTrue(result.stream().anyMatch(u -> u.getFirstName().equals("John")));
        assertTrue(result.stream().anyMatch(u -> u.getFirstName().equals("Mike")));

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveContractors_WithNoActiveContractors_ReturnsEmptyList() {
        List<Users> allActiveUsers = Arrays.asList(
                activeSalesperson,
                activeOwner,
                activeCustomer
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveContractors();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveContractors_WithNoActiveUsers_ReturnsEmptyList() {
        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        List<UserResponseModel> result = userService.getActiveContractors();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveContractors_DoesNotIncludeInactiveContractors() {
        List<Users> allActiveUsers = Arrays.asList(activeContractor);

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveContractors();

        assertEquals(1, result.size());
        assertFalse(result.stream().anyMatch(u -> 
                u.getPrimaryEmail().equals("bob.builder@example.com")));

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveContractors_FiltersOutOtherRoles() {
        List<Users> allActiveUsers = Arrays.asList(
                activeContractor,
                activeSalesperson,
                activeOwner,
                activeCustomer
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveContractors();

        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(u -> u.getUserRole() == UserRole.CONTRACTOR));
        assertFalse(result.stream().anyMatch(u -> u.getUserRole() == UserRole.SALESPERSON));
        assertFalse(result.stream().anyMatch(u -> u.getUserRole() == UserRole.OWNER));
        assertFalse(result.stream().anyMatch(u -> u.getUserRole() == UserRole.CUSTOMER));

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    // ========================== GET ACTIVE SALESPERSONS TESTS ==========================

    @Test
    void getActiveSalespersons_WithActiveSalespersons_ReturnsOnlyActiveSalespersons() {
        List<Users> allActiveUsers = Arrays.asList(
                activeContractor,
                activeSalesperson,
                activeOwner,
                activeCustomer
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
        assertEquals("Salesperson", result.get(0).getLastName());
        assertEquals(UserRole.SALESPERSON, result.get(0).getUserRole());
        assertEquals("jane.sales@example.com", result.get(0).getPrimaryEmail());

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveSalespersons_WithMultipleActiveSalespersons_ReturnsAll() {
        Users secondSalesperson = new Users();
        secondSalesperson.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        secondSalesperson.setFirstName("Tom");
        secondSalesperson.setLastName("Seller");
        secondSalesperson.setPrimaryEmail("tom.seller@example.com");
        secondSalesperson.setUserRole(UserRole.SALESPERSON);
        secondSalesperson.setUserStatus(UserStatus.ACTIVE);

        List<Users> allActiveUsers = Arrays.asList(
                activeSalesperson,
                secondSalesperson,
                activeContractor
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.getUserRole() == UserRole.SALESPERSON));
        assertTrue(result.stream().anyMatch(u -> u.getFirstName().equals("Jane")));
        assertTrue(result.stream().anyMatch(u -> u.getFirstName().equals("Tom")));

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveSalespersons_WithNoActiveSalespersons_ReturnsEmptyList() {
        List<Users> allActiveUsers = Arrays.asList(
                activeContractor,
                activeOwner,
                activeCustomer
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveSalespersons_WithNoActiveUsers_ReturnsEmptyList() {
        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveSalespersons_DoesNotIncludeInactiveSalespersons() {
        List<Users> allActiveUsers = Arrays.asList(activeSalesperson);

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertEquals(1, result.size());
        assertFalse(result.stream().anyMatch(u -> 
                u.getPrimaryEmail().equals("sarah.seller@example.com")));

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void getActiveSalespersons_FiltersOutOtherRoles() {
        List<Users> allActiveUsers = Arrays.asList(
                activeContractor,
                activeSalesperson,
                activeOwner,
                activeCustomer
        );

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(allActiveUsers);

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(u -> u.getUserRole() == UserRole.SALESPERSON));
        assertFalse(result.stream().anyMatch(u -> u.getUserRole() == UserRole.CONTRACTOR));
        assertFalse(result.stream().anyMatch(u -> u.getUserRole() == UserRole.OWNER));
        assertFalse(result.stream().anyMatch(u -> u.getUserRole() == UserRole.CUSTOMER));

        verify(usersRepository, times(1)).findByUserStatus(UserStatus.ACTIVE);
    }

    // ========================== EDGE CASE TESTS ==========================

    @Test
    void getActiveContractors_WithNullFields_HandlesGracefully() {
        Users contractorWithNulls = new Users();
        contractorWithNulls.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        contractorWithNulls.setFirstName("Test");
        contractorWithNulls.setLastName("User");
        contractorWithNulls.setPrimaryEmail("test@example.com");
        contractorWithNulls.setSecondaryEmail(null);
        contractorWithNulls.setPhone(null);
        contractorWithNulls.setUserRole(UserRole.CONTRACTOR);
        contractorWithNulls.setUserStatus(UserStatus.ACTIVE);

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(Arrays.asList(contractorWithNulls));

        List<UserResponseModel> result = userService.getActiveContractors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getSecondaryEmail());
        assertNull(result.get(0).getPhone());
    }

    @Test
    void getActiveSalespersons_WithNullFields_HandlesGracefully() {
        Users salespersonWithNulls = new Users();
        salespersonWithNulls.setUserIdentifier(UserIdentifier.fromString(UUID.randomUUID().toString()));
        salespersonWithNulls.setFirstName("Test");
        salespersonWithNulls.setLastName("User");
        salespersonWithNulls.setPrimaryEmail("test@example.com");
        salespersonWithNulls.setSecondaryEmail(null);
        salespersonWithNulls.setPhone(null);
        salespersonWithNulls.setUserRole(UserRole.SALESPERSON);
        salespersonWithNulls.setUserStatus(UserStatus.ACTIVE);

        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(Arrays.asList(salespersonWithNulls));

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getSecondaryEmail());
        assertNull(result.get(0).getPhone());
    }

    @Test
    void getActiveContractors_MapsAllFieldsCorrectly() {
        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(Arrays.asList(activeContractor));

        List<UserResponseModel> result = userService.getActiveContractors();

        assertEquals(1, result.size());
        UserResponseModel contractor = result.get(0);
        
        assertNotNull(contractor.getUserIdentifier());
        assertEquals("John", contractor.getFirstName());
        assertEquals("Contractor", contractor.getLastName());
        assertEquals("john.contractor@example.com", contractor.getPrimaryEmail());
        assertEquals("514-555-0001", contractor.getPhone());
        assertEquals(UserRole.CONTRACTOR, contractor.getUserRole());
        assertEquals(UserStatus.ACTIVE, contractor.getUserStatus());
    }

    @Test
    void getActiveSalespersons_MapsAllFieldsCorrectly() {
        when(usersRepository.findByUserStatus(UserStatus.ACTIVE))
                .thenReturn(Arrays.asList(activeSalesperson));

        List<UserResponseModel> result = userService.getActiveSalespersons();

        assertEquals(1, result.size());
        UserResponseModel salesperson = result.get(0);
        
        assertNotNull(salesperson.getUserIdentifier());
        assertEquals("Jane", salesperson.getFirstName());
        assertEquals("Salesperson", salesperson.getLastName());
        assertEquals("jane.sales@example.com", salesperson.getPrimaryEmail());
        assertEquals("514-555-0002", salesperson.getPhone());
        assertEquals(UserRole.SALESPERSON, salesperson.getUserRole());
        assertEquals(UserStatus.ACTIVE, salesperson.getUserStatus());
    }
}
