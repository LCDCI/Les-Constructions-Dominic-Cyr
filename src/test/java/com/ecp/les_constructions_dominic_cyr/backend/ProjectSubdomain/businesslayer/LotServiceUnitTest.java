package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LotServiceUnitTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private LotServiceImpl lotService;

    private Users testCustomer;
    private String testCustomerId;
    private Project testProject;
    private String testProjectIdentifier;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID().toString();
        testCustomer = new Users();
        testCustomer.setUserIdentifier(UserIdentifier.fromString(testCustomerId));
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Customer");
        testCustomer.setPrimaryEmail("john.customer@test.com");
        testCustomer.setUserRole(UserRole.CUSTOMER);
        testCustomer.setUserStatus(UserStatus.ACTIVE);

        // Setup test project
        testProjectIdentifier = "test-project-001";
        testProject = new Project();
        testProject.setProjectIdentifier(testProjectIdentifier);
        testProject.setProjectName("Test Project");
    }

    private Lot buildLotEntity(String lotId, String lotNumber, String civicAddress, float price, String dimsSqFt, String dimsSqM, LotStatus status) {
        var id = new LotIdentifier(lotId);
        return new Lot(id, lotNumber, civicAddress, price, dimsSqFt, dimsSqM, status);
    }

    // ==== GET ALL ====
    @Test
    public void whenLotsExist_thenReturnAllLots() {
        // arrange
        var e1 = buildLotEntity("db43c148-68de-4882-818a-d15dc8d5fcdb", "Lot-109", "Chicoutimi, QC", 160000.0f, "8775", "815", LotStatus.SOLD);
        var e2 = buildLotEntity("adb6f5b7-e036-49cf-899e-a39dcaecd91f", "Lot-110", "Baie-Comeau, QC", 145000.0f, "7500", "696", LotStatus.AVAILABLE);
        when(lotRepository.findAll()).thenReturn(List.of(e1, e2));

        // act
        List<LotResponseModel> list = lotService.getAllLots();

        // assert
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Chicoutimi, QC", list.get(0).getCivicAddress());
        verify(lotRepository, times(1)).findAll();
    }

    @Test
    public void whenNoLotsExist_thenReturnEmptyList() {
        when(lotRepository.findAll()).thenReturn(List.of());

        List<LotResponseModel> list = lotService.getAllLots();

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(lotRepository, times(1)).findAll();
    }

    // ==== GET BY ID ====
    @Test
    public void whenGetByIdFound_thenReturnDto() {
        String id = "3cf1b7e2-25fa-4a1b-9fe6-6a582f3d6c7a";
        var entity = buildLotEntity(id, "Lot-123", "FoundLoc", 150f, "1500", "139.4", LotStatus.AVAILABLE);
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(entity);

        LotResponseModel resp = lotService.getLotById(id);

        assertNotNull(resp);
        assertEquals(id, resp.getLotId());
        assertEquals("FoundLoc", resp.getCivicAddress());
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(UUID.fromString(id));
    }

    @Test
    public void whenGetByIdNotFound_thenThrowNotFound() {
        String id = "ec8a60ea-7a5b-4a2a-9e92-08ba64d5b20e";
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> lotService.getLotById(id));
        assertTrue(ex.getMessage().contains("Unknown Lot Id"));
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(UUID.fromString(id));
    }

    @Test
    public void whenGetByIdWithAssignedCustomer_thenReturnDtoWithCustomerInfo() {
        String id = "2c1788f0-24af-4a5a-8e19-5bb9cb8cfef6";
        var entity = buildLotEntity(id, "Lot-124", "CustomerLoc", 200f, "2000", "185.8", LotStatus.SOLD);
        entity.setAssignedUsers(List.of(testCustomer));
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(entity);

        LotResponseModel resp = lotService.getLotById(id);

        assertNotNull(resp);
        assertEquals(id, resp.getLotId());
        assertNotNull(resp.getAssignedUsers());
        assertEquals(1, resp.getAssignedUsers().size());
        assertEquals(testCustomerId, resp.getAssignedUsers().get(0).getUserId());
        assertEquals("John Customer", resp.getAssignedUsers().get(0).getFullName());
    }

    // ==== CREATE ====
    @Test
    public void whenValidCreate_thenReturnCreatedDto() {
        // arrange request
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-456");
        req.setCivicAddress("CreateLoc");
        req.setPrice(123f);
        req.setDimensionsSquareFeet("1230");
        req.setDimensionsSquareMeters("114.3");
        req.setLotStatus(LotStatus.AVAILABLE);

        // Mock project repository to return test project
        when(projectRepository.findByProjectIdentifier(testProjectIdentifier))
                .thenReturn(Optional.of(testProject));

        // repository.save should return the entity (service sets LotIdentifier before save)
        when(lotRepository.save(any(Lot.class))).thenAnswer(invocation -> {
            Lot arg = invocation.getArgument(0);
            if (arg.getLotIdentifier() == null) {
                arg.setLotIdentifier(new LotIdentifier(UUID.randomUUID().toString()));
            }
            return arg;
        });

        // act
        LotResponseModel resp = lotService.addLotToProject(testProjectIdentifier, req);

        // assert
        assertNotNull(resp);
        assertEquals("CreateLoc", resp.getCivicAddress());
        assertNotNull(resp.getLotId());
        verify(lotRepository, times(1)).save(any(Lot.class));
        verify(projectRepository, times(1)).findByProjectIdentifier(testProjectIdentifier);
    }

    @Test
    public void whenCreateWithCustomer_thenReturnCreatedDtoWithCustomer() {
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-457");
        req.setCivicAddress("CustomerCreateLoc");
        req.setPrice(150f);
        req.setDimensionsSquareFeet("1500");
        req.setDimensionsSquareMeters("139.4");
        req.setLotStatus(LotStatus.SOLD);
        req.setAssignedUserIds(List.of(testCustomerId));

        when(projectRepository.findByProjectIdentifier(testProjectIdentifier))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier_UserId(UUID.fromString(testCustomerId)))
                .thenReturn(Optional.of(testCustomer));
        when(lotRepository.save(any(Lot.class))).thenAnswer(invocation -> {
            Lot arg = invocation.getArgument(0);
            if (arg.getLotIdentifier() == null) {
                arg.setLotIdentifier(new LotIdentifier(UUID.randomUUID().toString()));
            }
            return arg;
        });

        LotResponseModel resp = lotService.addLotToProject(testProjectIdentifier, req);

        assertNotNull(resp);
        assertEquals("CustomerCreateLoc", resp.getCivicAddress());
        assertNotNull(resp.getAssignedUsers());
        assertEquals(1, resp.getAssignedUsers().size());
        assertEquals(testCustomerId, resp.getAssignedUsers().get(0).getUserId());
        assertEquals("John Customer", resp.getAssignedUsers().get(0).getFullName());
    }

    @Test
    public void whenCreateWithNonCustomerRole_thenThrowInvalidInput() {
        Users contractor = new Users();
        contractor.setUserIdentifier(UserIdentifier.fromString(testCustomerId));
        contractor.setUserRole(UserRole.CONTRACTOR);

        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-458");
        req.setCivicAddress("ContractorLoc");
        req.setPrice(100f);
        req.setDimensionsSquareFeet("1000");
        req.setDimensionsSquareMeters("92.9");
        req.setLotStatus(LotStatus.AVAILABLE);
        req.setAssignedUserIds(List.of(testCustomerId));

        when(projectRepository.findByProjectIdentifier(testProjectIdentifier))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier_UserId(UUID.fromString(testCustomerId)))
                .thenReturn(Optional.of(contractor));
        when(lotRepository.save(any(Lot.class))).thenAnswer(invocation -> {
            Lot arg = invocation.getArgument(0);
            if (arg.getLotIdentifier() == null) {
                arg.setLotIdentifier(new LotIdentifier(UUID.randomUUID().toString()));
            }
            return arg;
        });

        // This should not throw anymore since we support all roles
        assertDoesNotThrow(() -> lotService.addLotToProject(testProjectIdentifier, req));
    }

    @Test
    public void whenCreateWithNullReq_thenThrowsNPEorIllegalArg() {
        // Negative: if caller passes null, behavior depends on implementation - assert it throws
        assertThrows(Exception.class, () -> lotService.addLotToProject(testProjectIdentifier, null));
    }

    // ==== UPDATE ====
    @Test
    public void whenUpdateExisting_thenReturnUpdatedDto() {
        String id = "4f1c5f66-2c8f-4e8a-a4ef-5c4cc3a0f4c1";
        var stored = buildLotEntity(id, "Lot-789", "OldLoc", 50f, "500", "46.5", LotStatus.AVAILABLE);

        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(stored);
        when(lotRepository.save(stored)).thenReturn(stored);

        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-789-Updated");
        req.setCivicAddress("NewLoc");
        req.setPrice(60f);
        req.setDimensionsSquareFeet("600");
        req.setDimensionsSquareMeters("55.7");
        req.setLotStatus(LotStatus.SOLD);

        LotResponseModel resp = lotService.updateLot(req, id);

        assertNotNull(resp);
        assertEquals("NewLoc", resp.getCivicAddress());
        assertEquals(LotStatus.SOLD, resp.getLotStatus());
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(UUID.fromString(id));
        verify(lotRepository, times(1)).save(stored);
    }

    @Test
    public void whenUpdateWithCustomerAssignment_thenReturnUpdatedDtoWithCustomer() {
        String id = "9a12c63b-1169-498b-96f7-6a78744b1b6f";
        var stored = buildLotEntity(id, "Lot-800", "OldLoc2", 70f, "700", "65.0", LotStatus.AVAILABLE);

        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(stored);
        when(usersRepository.findByUserIdentifier_UserId(UUID.fromString(testCustomerId)))
                .thenReturn(Optional.of(testCustomer));
        when(lotRepository.save(stored)).thenReturn(stored);

        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-800");
        req.setCivicAddress("OldLoc2");
        req.setPrice(70f);
        req.setDimensionsSquareFeet("700");
        req.setDimensionsSquareMeters("65.0");
        req.setLotStatus(LotStatus.SOLD);
        req.setAssignedUserIds(List.of(testCustomerId));

        LotResponseModel resp = lotService.updateLot(req, id);

        assertNotNull(resp);
        assertNotNull(resp.getAssignedUsers());
        assertEquals(1, resp.getAssignedUsers().size());
        assertEquals(testCustomerId, resp.getAssignedUsers().get(0).getUserId());
        assertEquals("John Customer", resp.getAssignedUsers().get(0).getFullName());
    }

    @Test
    public void whenUpdateRemoveCustomerAssignment_thenReturnDtoWithoutCustomer() {
        String id = "b5e5a81d-2e24-4247-9d75-e41597a0cfd1";
        var stored = buildLotEntity(id, "Lot-801", "OldLoc3", 80f, "800", "74.3", LotStatus.SOLD);
        stored.setAssignedUsers(List.of(testCustomer));

        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(stored);
        when(lotRepository.save(stored)).thenReturn(stored);

        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-801");
        req.setCivicAddress("OldLoc3");
        req.setPrice(80f);
        req.setDimensionsSquareFeet("800");
        req.setDimensionsSquareMeters("74.3");
        req.setLotStatus(LotStatus.AVAILABLE);
        req.setAssignedUserIds(List.of()); // Remove assignment

        LotResponseModel resp = lotService.updateLot(req, id);

        assertNotNull(resp);
        assertTrue(resp.getAssignedUsers() == null || resp.getAssignedUsers().isEmpty());
    }

    @Test
    public void whenUpdateNonExisting_thenThrowNotFound() {
        String id = "1a6f0f80-5c24-457b-9d61-5649c2f7a8d7";
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(null);
        LotRequestModel req = new LotRequestModel();
        assertThrows(NotFoundException.class, () -> lotService.updateLot(req, id));
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(UUID.fromString(id));
    }

    // ==== DELETE ====
    @Test
    public void whenDeleteExisting_thenDeletes() {
        String id = "6c2d9c67-391f-4b7c-86c1-7be1df4f2e0b";
        var stored = buildLotEntity(id, "Lot-999", "Rem", 10f, "100", "9.3", LotStatus.AVAILABLE);
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(stored);

        assertDoesNotThrow(() -> lotService.deleteLot(id));
        verify(lotRepository, times(1)).delete(stored);
    }

    @Test
    public void whenDeleteNonExisting_thenThrowNotFound() {
        String id = "a6d4b9a1-6f59-4b1b-93a2-75bfe3b2bd3a";
        when(lotRepository.findByLotIdentifier_LotId(UUID.fromString(id))).thenReturn(null);
        assertThrows(NotFoundException.class, () -> lotService.deleteLot(id));
        verify(lotRepository, times(1)).findByLotIdentifier_LotId(UUID.fromString(id));
    }

    // ==== CUSTOMER VALIDATION ====
    @Test
    public void whenAssignCustomerNotFound_thenThrowNotFound() {
        String nonExistentCustomerId = UUID.randomUUID().toString();
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-460");
        req.setCivicAddress("NotFoundLoc");
        req.setPrice(100f);
        req.setDimensionsSquareFeet("1000");
        req.setDimensionsSquareMeters("92.9");
        req.setLotStatus(LotStatus.AVAILABLE);
        req.setAssignedUserIds(List.of(nonExistentCustomerId));

        when(projectRepository.findByProjectIdentifier(testProjectIdentifier))
                .thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier_UserId(UUID.fromString(nonExistentCustomerId)))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> lotService.addLotToProject(testProjectIdentifier, req));
    }

    @Test
    public void whenAssignInvalidCustomerId_thenThrowInvalidInput() {
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("Lot-461");
        req.setCivicAddress("InvalidIdLoc");
        req.setPrice(100f);
        req.setDimensionsSquareFeet("1000");
        req.setDimensionsSquareMeters("92.9");
        req.setLotStatus(LotStatus.AVAILABLE);
        req.setAssignedUserIds(List.of("invalid-uuid-format"));

        when(projectRepository.findByProjectIdentifier(testProjectIdentifier))
                .thenReturn(Optional.of(testProject));

        assertThrows(InvalidInputException.class, () -> lotService.addLotToProject(testProjectIdentifier, req));
    }
}
