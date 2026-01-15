package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.IndividualProjectMetadataServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project.IndividualProjectResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ProjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IndividualProjectMetadataServiceImplUnitTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private IndividualProjectMetadataServiceImpl individualProjectMetadataService;

    private Project testProject;
        private Users contractorUser;
        private Users salespersonUser;
        private Users customerUser;
        private Users ownerUser;
    private Lot testLot;
        private UUID contractorUUID;
        private UUID salespersonUUID;
        private UUID customerUUID;
        private static final String OWNER_AUTH0_ID = "auth0|owner-user";

    @BeforeEach
    void setUp() {
        contractorUUID = UUID.randomUUID();
        salespersonUUID = UUID.randomUUID();
        customerUUID = UUID.randomUUID();

        testProject = new Project();
        testProject.setProjectIdentifier("proj-metadata-001");
        testProject.setProjectName("Metadata Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setStartDate(LocalDate.of(2025, 1, 1));
        testProject.setEndDate(LocalDate.of(2025, 12, 31));
        testProject.setCompletionDate(null);
        testProject.setPrimaryColor("#FFFFFF");
        testProject.setTertiaryColor("#000000");
        testProject.setBuyerColor("#FF0000");
        testProject.setBuyerName("Test Buyer");
        testProject.setImageIdentifier("main-img-001");
        testProject.setLocation("Montreal, QC");
        testProject.setProgressPercentage(50);
        testProject.setContractorIds(new ArrayList<>(Arrays.asList(contractorUUID.toString())));
        testProject.setSalespersonIds(new ArrayList<>(Arrays.asList(salespersonUUID.toString())));
        testProject.setCustomerId(customerUUID.toString());
        testProject.setLotIdentifiers(new ArrayList<>(Arrays.asList("lot-001")));

        contractorUser = new Users();
        contractorUser.setUserIdentifier(createUserIdentifier(contractorUUID));
        contractorUser.setFirstName("John");
        contractorUser.setLastName("Contractor");
        contractorUser.setPrimaryEmail("john.contractor@example.com");
        contractorUser.setPhone("555-1234");
        contractorUser.setUserRole(UserRole.CONTRACTOR);

        salespersonUser = new Users();
        salespersonUser.setUserIdentifier(createUserIdentifier(salespersonUUID));
        salespersonUser.setFirstName("Jane");
        salespersonUser.setLastName("Sales");
        salespersonUser.setPrimaryEmail("jane.sales@example.com");
        salespersonUser.setPhone("555-5678");
        salespersonUser.setUserRole(UserRole.SALESPERSON);

        customerUser = new Users();
        customerUser.setUserIdentifier(createUserIdentifier(customerUUID));
        customerUser.setFirstName("Bob");
        customerUser.setLastName("Customer");
        customerUser.setPrimaryEmail("bob.customer@example.com");
        customerUser.setPhone("555-9012");
        customerUser.setUserRole(UserRole.CUSTOMER);

                ownerUser = new Users();
                ownerUser.setUserIdentifier(createUserIdentifier(UUID.randomUUID()));
                ownerUser.setFirstName("Olivia");
                ownerUser.setLastName("Owner");
                ownerUser.setPrimaryEmail("owner@example.com");
                ownerUser.setUserRole(UserRole.OWNER);
                ownerUser.setAuth0UserId(OWNER_AUTH0_ID);

        testLot = new Lot(new LotIdentifier("lot-001"), "Downtown Location", 500000f, "50x100", LotStatus.AVAILABLE);

                lenient().when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(ownerUser));
    }

    private UserIdentifier createUserIdentifier(UUID uuid) {
        return new UserIdentifier() {
            @Override
            public UUID getUserId() {
                return uuid;
            }
        };
    }

    @Test
    void getProjectMetadata_WhenProjectExistsWithAllUsers_ReturnsCompleteMetadata() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByAuth0UserId(OWNER_AUTH0_ID)).thenReturn(Optional.of(ownerUser));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenAnswer(invocation -> {
                    UserIdentifier uid = invocation.getArgument(0);
                    if (uid.getUserId().equals(contractorUUID)) return Optional.of(contractorUser);
                    if (uid.getUserId().equals(salespersonUUID)) return Optional.of(salespersonUser);
                    if (uid.getUserId().equals(customerUUID)) return Optional.of(customerUser);
                    return Optional.empty();
                });

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("proj-metadata-001", result.getProjectIdentifier());
        assertEquals("Metadata Test Project", result.getProjectName());
        assertEquals("Test Description", result.getProjectDescription());
        assertEquals(ProjectStatus.IN_PROGRESS, result.getStatus());
        assertEquals(LocalDate.of(2025, 1, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), result.getEndDate());
        assertNull(result.getCompletionDate());
        assertEquals("#FFFFFF", result.getPrimaryColor());
        assertEquals("#000000", result.getTertiaryColor());
        assertEquals("#FF0000", result.getBuyerColor());
        assertEquals("Test Buyer", result.getBuyerName());
        assertEquals("main-img-001", result.getImageIdentifier());
        assertEquals("Montreal, QC", result.getLocation());
        assertEquals(50, result.getProgressPercentage());

        // Verify assigned users
        assertNotNull(result.getAssignedUsers());
        assertNotNull(result.getAssignedUsers().getContractors());
        assertEquals(1, result.getAssignedUsers().getContractors().size());
        assertEquals("John", result.getAssignedUsers().getContractors().get(0).getFirstName());
        assertEquals("Contractor", result.getAssignedUsers().getContractors().get(0).getLastName());
        assertEquals("john.contractor@example.com", result.getAssignedUsers().getContractors().get(0).getPrimaryEmail());
        assertEquals("555-1234", result.getAssignedUsers().getContractors().get(0).getPhone());
        assertEquals("CONTRACTOR", result.getAssignedUsers().getContractors().get(0).getRole());

        assertNotNull(result.getAssignedUsers().getSalespersons());
        assertEquals(1, result.getAssignedUsers().getSalespersons().size());
        assertEquals("Jane", result.getAssignedUsers().getSalespersons().get(0).getFirstName());
        assertEquals("Sales", result.getAssignedUsers().getSalespersons().get(0).getLastName());
        testProject.setContractorIds(new ArrayList<>());
        testProject.setSalespersonIds(new ArrayList<>());
        testProject.setContractorIds(new ArrayList<>());
        testProject.setSalespersonIds(new ArrayList<>());
        testProject.setContractorIds(new ArrayList<>());
        testProject.setSalespersonIds(new ArrayList<>());
        testProject.setContractorIds(new ArrayList<>(Arrays.asList("invalid-uuid")));
        testProject.setSalespersonIds(new ArrayList<>());
        testProject.setContractorIds(new ArrayList<>(Arrays.asList(contractorUUID.toString())));
        testProject.setSalespersonIds(new ArrayList<>(Arrays.asList("invalid-uuid")));

        assertNotNull(result.getAssignedUsers().getCustomer());
        assertEquals("Bob", result.getAssignedUsers().getCustomer().getFirstName());
        assertEquals("Customer", result.getAssignedUsers().getCustomer().getLastName());

        verify(projectRepository, times(1)).findByProjectIdentifier(eq("proj-metadata-001"));
        verify(usersRepository, times(1)).findByAuth0UserId(OWNER_AUTH0_ID);
        verify(usersRepository, times(3)).findById(any(UserIdentifier.class));
    }

    @Test
    void getProjectMetadata_WhenProjectNotFound_ThrowsProjectNotFoundException() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("non-existent")))
                .thenReturn(Optional.empty());

        // Act & Assert
        ProjectNotFoundException exception = assertThrows(
                ProjectNotFoundException.class,
                () -> individualProjectMetadataService.getProjectMetadata("non-existent", OWNER_AUTH0_ID)
        );

        assertEquals("Project not found with identifier: non-existent", exception.getMessage());

        verify(projectRepository, times(1)).findByProjectIdentifier(eq("non-existent"));
        verify(usersRepository, never()).findById(any());
        verify(lotRepository, never()).findByLotIdentifier_LotId(any());
    }

    @Test
    void getProjectMetadata_WhenProjectHasNoAssignedUsers_ReturnsNullUsers() {
        // Arrange
        testProject.setContractorIds(new ArrayList<>());
        testProject.setSalespersonIds(new ArrayList<>());
        testProject.setCustomerId(null);

        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByAuth0UserId(OWNER_AUTH0_ID)).thenReturn(Optional.of(ownerUser));

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAssignedUsers());
        assertTrue(result.getAssignedUsers().getContractors().isEmpty());
        assertTrue(result.getAssignedUsers().getSalespersons().isEmpty());
        assertNull(result.getAssignedUsers().getCustomer());

        verify(usersRepository, never()).findById(any());
    }

    @Test
    void getProjectMetadata_WhenUserNotFoundInRepository_ReturnsNullForThatUser() {
        // Arrange
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByAuth0UserId(OWNER_AUTH0_ID)).thenReturn(Optional.of(ownerUser));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAssignedUsers());
        assertTrue(result.getAssignedUsers().getContractors().isEmpty());
        assertTrue(result.getAssignedUsers().getSalespersons().isEmpty());
        assertNull(result.getAssignedUsers().getCustomer());

        verify(usersRepository, times(3)).findById(any(UserIdentifier.class));
    }

    @Test
    void getProjectMetadata_WhenProjectHasExplicitLocation_UsesProjectLocation() {
        // Arrange
        testProject.setLocation("Explicit Location");
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByAuth0UserId(OWNER_AUTH0_ID)).thenReturn(Optional.of(ownerUser));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Explicit Location", result.getLocation());

        verify(lotRepository, never()).findByLotIdentifier_LotId(any());
    }

    @Test
    void getProjectMetadata_WhenNoLocationButHasLot_UsesLotLocation() {
        // Arrange
        testProject.setLocation(null);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotId(eq("lot-001")))
                .thenReturn(testLot);
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Downtown Location", result.getLocation());

        verify(lotRepository, times(1)).findByLotIdentifier_LotId(eq("lot-001"));
    }

    @Test
    void getProjectMetadata_WhenEmptyLocationButHasLot_UsesLotLocation() {
        // Arrange
        testProject.setLocation("");
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotId(eq("lot-001")))
                .thenReturn(testLot);
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Downtown Location", result.getLocation());

        verify(lotRepository, times(1)).findByLotIdentifier_LotId(eq("lot-001"));
    }

    @Test
    void getProjectMetadata_WhenNoLocationAndNoLots_ReturnsDefaultLocation() {
        // Arrange
        testProject.setLocation(null);
        testProject.setLotIdentifiers(new ArrayList<>());
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Location not specified", result.getLocation());

        verify(lotRepository, never()).findByLotIdentifier_LotId(any());
    }

    @Test
    void getProjectMetadata_WhenNoLocationAndLotIsNull_ReturnsDefaultLocation() {
        // Arrange
        testProject.setLocation(null);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotId(eq("lot-001")))
                .thenReturn(null);
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Location not specified", result.getLocation());

        verify(lotRepository, times(1)).findByLotIdentifier_LotId(eq("lot-001"));
    }

    @Test
    void getProjectMetadata_WhenNoLocationAndLotHasNoLocation_ReturnsDefaultLocation() {
        // Arrange
        testProject.setLocation(null);
        testLot.setLocation(null);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(lotRepository.findByLotIdentifier_LotId(eq("lot-001")))
                .thenReturn(testLot);
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Location not specified", result.getLocation());

        verify(lotRepository, times(1)).findByLotIdentifier_LotId(eq("lot-001"));
    }

    @Test
    void getProjectMetadata_WhenNoLocationAndLotIdentifiersIsNull_ReturnsDefaultLocation() {
        // Arrange
        testProject.setLocation(null);
        testProject.setLotIdentifiers(null);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Location not specified", result.getLocation());

        verify(lotRepository, never()).findByLotIdentifier_LotId(any());
    }

    @Test
    void getProjectMetadata_WhenUserNotAssignedToProject_ThrowsForbidden() {
        // Arrange
        Users unrelatedUser = new Users();
        unrelatedUser.setUserIdentifier(createUserIdentifier(UUID.randomUUID()));
        unrelatedUser.setUserRole(UserRole.CONTRACTOR);
        unrelatedUser.setAuth0UserId("auth0|unrelated");

        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByAuth0UserId("auth0|unrelated"))
                .thenReturn(Optional.of(unrelatedUser));

        // Act & Assert
        assertThrows(
                com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.ForbiddenAccessException.class,
                () -> individualProjectMetadataService.getProjectMetadata("proj-metadata-001", "auth0|unrelated")
        );
    }

    @Test
    void getProjectMetadata_WhenOnlyContractorAssigned_ReturnsOnlyContractor() {
        // Arrange
        testProject.setSalespersonIds(new ArrayList<>());
        testProject.setCustomerId(null);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.of(contractorUser));

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertFalse(result.getAssignedUsers().getContractors().isEmpty());
        assertTrue(result.getAssignedUsers().getSalespersons().isEmpty());
        assertNull(result.getAssignedUsers().getCustomer());

        verify(usersRepository, times(1)).findById(any(UserIdentifier.class));
    }

    @Test
    void getProjectMetadata_WhenOnlySalespersonAssigned_ReturnsOnlySalesperson() {
        // Arrange
        testProject.setContractorIds(new ArrayList<>());
        testProject.setCustomerId(null);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.of(salespersonUser));

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAssignedUsers().getContractors().isEmpty());
        assertFalse(result.getAssignedUsers().getSalespersons().isEmpty());
        assertNull(result.getAssignedUsers().getCustomer());

        verify(usersRepository, times(1)).findById(any(UserIdentifier.class));
    }

    @Test
    void getProjectMetadata_WhenOnlyCustomerAssigned_ReturnsOnlyCustomer() {
        // Arrange
        testProject.setContractorIds(new ArrayList<>());
        testProject.setSalespersonIds(new ArrayList<>());
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.of(customerUser));

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAssignedUsers().getContractors().isEmpty());
        assertTrue(result.getAssignedUsers().getSalespersons().isEmpty());
        assertNotNull(result.getAssignedUsers().getCustomer());

        verify(usersRepository, times(1)).findById(any(UserIdentifier.class));
    }

    @Test
    void getProjectMetadata_WhenInvalidUUIDForUser_ReturnsNullForThatUser() {
        // Arrange
        testProject.setContractorIds(new ArrayList<>(Arrays.asList("invalid-uuid")));
        testProject.setSalespersonIds(new ArrayList<>());
        testProject.setCustomerId(null);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAssignedUsers());
        assertTrue(result.getAssignedUsers().getContractors().isEmpty());

        verify(usersRepository, never()).findById(any());
    }

    @Test
    void getProjectMetadata_WhenMixOfValidAndInvalidUUIDs_ReturnsOnlyValidUsers() {
        // Arrange
        testProject.setContractorIds(new ArrayList<>(Arrays.asList(contractorUUID.toString())));
        testProject.setSalespersonIds(new ArrayList<>(Arrays.asList("invalid-uuid")));
        testProject.setCustomerId(customerUUID.toString());

        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenAnswer(invocation -> {
                    UserIdentifier uid = invocation.getArgument(0);
                    if (uid.getUserId().equals(contractorUUID)) return Optional.of(contractorUser);
                    if (uid.getUserId().equals(customerUUID)) return Optional.of(customerUser);
                    return Optional.empty();
                });

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertFalse(result.getAssignedUsers().getContractors().isEmpty());
        assertTrue(result.getAssignedUsers().getSalespersons().isEmpty());
        assertNotNull(result.getAssignedUsers().getCustomer());

        verify(usersRepository, times(2)).findById(any(UserIdentifier.class));
    }

    @Test
    void getProjectMetadata_WhenProjectHasCompletionDate_ReturnsCompletionDate() {
        // Arrange
        LocalDate completionDate = LocalDate.of(2025, 6, 15);
        testProject.setCompletionDate(completionDate);
        testProject.setStatus(ProjectStatus.COMPLETED);

        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals(completionDate, result.getCompletionDate());
        assertEquals(ProjectStatus.COMPLETED, result.getStatus());
    }

    @Test
    void getProjectMetadata_WhenDifferentProjectStatus_ReturnsCorrectStatus() {
        // Arrange
        testProject.setStatus(ProjectStatus.PLANNED);
        when(projectRepository.findByProjectIdentifier(eq("proj-metadata-001")))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findById(any(UserIdentifier.class)))
                .thenReturn(Optional.empty());

        // Act
        IndividualProjectResponseModel result = individualProjectMetadataService.getProjectMetadata("proj-metadata-001", OWNER_AUTH0_ID);

        // Assert
        assertNotNull(result);
        assertEquals(ProjectStatus.PLANNED, result.getStatus());
    }
}