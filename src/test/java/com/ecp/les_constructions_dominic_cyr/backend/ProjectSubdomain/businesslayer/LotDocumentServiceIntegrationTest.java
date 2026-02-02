package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LotDocument.LotDocumentService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument.LotDocument;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument.LotDocumentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument.LotDocumentResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class LotDocumentServiceIntegrationTest {

    @Autowired
    private LotDocumentService lotDocumentService;

    @Autowired
    private LotDocumentRepository lotDocumentRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @MockBean
    private WebClient.Builder webClientBuilder;

    private Lot testLot;
    private Users ownerUser;
    private Users contractorUser;
    private Users customerUser;
    private Users outsiderUser;
    private Project testProject;

    @BeforeEach
    public void setUp() {
        // Create test project
        testProject = new Project();
        testProject.setProjectName("Test Project Documents");
        testProject.setProjectIdentifier("proj-test-docs-" + UUID.randomUUID().toString().substring(0, 8));
        testProject = projectRepository.save(testProject);

        // Create test lot
        testLot = new Lot(
            new LotIdentifier(UUID.randomUUID().toString()),
                "Lot Test Docs",
                "123 Test St",
                100000f,
                "1000",
                "92.9",
                LotStatus.AVAILABLE
        );
        testLot.setProject(testProject);
        testLot = lotRepository.save(testLot);

        // Create owner user
        ownerUser = new Users();
        ownerUser.setUserIdentifier(UserIdentifier.newId());
        ownerUser.setFirstName("Owner");
        ownerUser.setLastName("User");
        ownerUser.setPrimaryEmail("owner@test.com");
        ownerUser.setUserRole(UserRole.OWNER);
        ownerUser.setUserStatus(UserStatus.ACTIVE);
        ownerUser.setAuth0UserId("auth0|owner-" + UUID.randomUUID());
        ownerUser = usersRepository.save(ownerUser);

        // Create contractor user assigned to lot
        contractorUser = new Users();
        contractorUser.setUserIdentifier(UserIdentifier.newId());
        contractorUser.setFirstName("Contractor");
        contractorUser.setLastName("User");
        contractorUser.setPrimaryEmail("contractor@test.com");
        contractorUser.setUserRole(UserRole.CONTRACTOR);
        contractorUser.setUserStatus(UserStatus.ACTIVE);
        contractorUser.setAuth0UserId("auth0|contractor-" + UUID.randomUUID());
        contractorUser = usersRepository.save(contractorUser);

        // Create customer user assigned to lot
        customerUser = new Users();
        customerUser.setUserIdentifier(UserIdentifier.newId());
        customerUser.setFirstName("Customer");
        customerUser.setLastName("User");
        customerUser.setPrimaryEmail("customer@test.com");
        customerUser.setUserRole(UserRole.CUSTOMER);
        customerUser.setUserStatus(UserStatus.ACTIVE);
        customerUser.setAuth0UserId("auth0|customer-" + UUID.randomUUID());
        customerUser = usersRepository.save(customerUser);

        // Create outsider user NOT assigned to lot
        outsiderUser = new Users();
        outsiderUser.setUserIdentifier(UserIdentifier.newId());
        outsiderUser.setFirstName("Outsider");
        outsiderUser.setLastName("User");
        outsiderUser.setPrimaryEmail("outsider@test.com");
        outsiderUser.setUserRole(UserRole.CONTRACTOR);
        outsiderUser.setUserStatus(UserStatus.ACTIVE);
        outsiderUser.setAuth0UserId("auth0|outsider-" + UUID.randomUUID());
        outsiderUser = usersRepository.save(outsiderUser);

        // Assign owner and contractor to lot
        testLot.getAssignedUsers().add(ownerUser);
        testLot.getAssignedUsers().add(contractorUser);
        testLot = lotRepository.save(testLot);

        // Setup WebClient mocks for file upload
        setupWebClientMocks();
    }

    @SuppressWarnings("unchecked")
    private void setupWebClientMocks() {
        // Create mock WebClient and related components
        WebClient webClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        // Mock the WebClient.Builder to return our mocked WebClient
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // Mock POST request flow for file upload
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Mock successful upload response with fileId
        String mockResponse = "{\"fileId\":\"mock-file-id-123\",\"url\":\"/files/mock-file-id-123\"}";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockResponse));
    }

    @Test
    public void testGetLotDocuments_LotExists_ReturnsEmptyList() {
        List<LotDocumentResponseModel> documents = lotDocumentService.getLotDocuments(
            testLot.getLotIdentifier().getLotId().toString(),
                null,
                "all"
        );

        assertNotNull(documents);
        assertEquals(0, documents.size());
    }

    @Test
    public void testGetLotDocuments_LotNotFound_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            lotDocumentService.getLotDocuments("nonexistent-lot", null, "all");
        });
    }

    @Test
    public void testUploadDocuments_OwnerCanUpload() {
        MultipartFile[] files = new MultipartFile[]{
                new MockMultipartFile(
                        "file",
                        "test-document.pdf",
                        "application/pdf",
                        "Test PDF content".getBytes()
                )
        };

        List<LotDocumentResponseModel> uploaded = lotDocumentService.uploadDocuments(
                testLot.getLotIdentifier().getLotId().toString(),
                files,
                ownerUser.getUserIdentifier().getUserId().toString()
        );

        assertNotNull(uploaded);
        assertEquals(1, uploaded.size());
        assertEquals("test-document.pdf", uploaded.get(0).getFileName());
        assertEquals("application/pdf", uploaded.get(0).getMimeType());
        assertFalse(uploaded.get(0).getIsImage());
    }

    @Test
    public void testUploadDocuments_ContractorCanUpload() {
        MultipartFile[] files = new MultipartFile[]{
                new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/jpeg",
                        "Test image content".getBytes()
                )
        };

        List<LotDocumentResponseModel> uploaded = lotDocumentService.uploadDocuments(
                testLot.getLotIdentifier().getLotId().toString(),
                files,
                contractorUser.getUserIdentifier().getUserId().toString()
        );

        assertNotNull(uploaded);
        assertEquals(1, uploaded.size());
        assertEquals("test-image.jpg", uploaded.get(0).getFileName());
        assertEquals("image/jpeg", uploaded.get(0).getMimeType());
        assertTrue(uploaded.get(0).getIsImage());
    }

    @Test
    public void testUploadDocuments_CustomerCannotUpload() {
        // Assign customer to lot first
        testLot.getAssignedUsers().add(customerUser);
        lotRepository.save(testLot);

        MultipartFile[] files = new MultipartFile[]{
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "Test content".getBytes()
                )
        };

        assertThrows(AccessDeniedException.class, () -> {
                lotDocumentService.uploadDocuments(
                    testLot.getLotIdentifier().getLotId().toString(),
                    files,
                    customerUser.getUserIdentifier().getUserId().toString()
            );
        });
    }

    @Test
    public void testUploadDocuments_FileServiceFailure_ThrowsException() {
        // Setup WebClient to simulate service failure
        WebClient webClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("File upload failed: Connection refused")));

        MultipartFile[] files = new MultipartFile[]{
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "Test content".getBytes()
                )
        };

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lotDocumentService.uploadDocuments(
                    testLot.getLotIdentifier().getLotId().toString(),
                    files,
                    ownerUser.getUserIdentifier().getUserId().toString()
            );
        });

        assertTrue(exception.getMessage().contains("Failed to upload file"));
    }

    @Test
    public void testUploadDocuments_OutsiderCannotUpload() {
        MultipartFile[] files = new MultipartFile[]{
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "Test content".getBytes()
                )
        };

        assertThrows(AccessDeniedException.class, () -> {
                lotDocumentService.uploadDocuments(
                    testLot.getLotIdentifier().getLotId().toString(),
                    files,
                    outsiderUser.getUserIdentifier().getUserId().toString()
            );
        });
    }

    @Test
    public void testDeleteDocument_UploaderCanDelete() {
        // Create a document uploaded by contractor
        LotDocument doc = new LotDocument(
                testLot,
                contractorUser,
                contractorUser.getFirstName() + " " + contractorUser.getLastName(),
                "test-file.pdf",
                "storage-key-123",
                "application/pdf",
                1024L,
                false
        );
        doc = lotDocumentRepository.save(doc);
        final LotDocument savedDoc = doc;

        // Contractor (uploader) can delete own document
        assertDoesNotThrow(() -> {
                lotDocumentService.deleteDocument(
                    testLot.getLotIdentifier().getLotId().toString(),
                    savedDoc.getId(),
                    contractorUser.getUserIdentifier().getUserId().toString()
            );
        });
    }

    @Test
    public void testDeleteDocument_OwnerCanDeleteAnyDocument() {
        // Create a document uploaded by contractor
        LotDocument doc = new LotDocument(
                testLot,
                contractorUser,
                contractorUser.getFirstName() + " " + contractorUser.getLastName(),
                "test-file.pdf",
                "storage-key-456",
                "application/pdf",
                1024L,
                false
        );
        doc = lotDocumentRepository.save(doc);
        final LotDocument savedDoc = doc;

        // Owner can delete contractor's document
        assertDoesNotThrow(() -> {
                lotDocumentService.deleteDocument(
                    testLot.getLotIdentifier().getLotId().toString(),
                    savedDoc.getId(),
                    ownerUser.getUserIdentifier().getUserId().toString()
            );
        });
    }

    @Test
    public void testDeleteDocument_NonUploaderNonOwnerCannotDelete() {
        // Assign another contractor to lot
        Users anotherContractor = new Users();
        anotherContractor.setUserIdentifier(UserIdentifier.newId());
        anotherContractor.setFirstName("Another");
        anotherContractor.setLastName("Contractor");
        anotherContractor.setPrimaryEmail("contractor2@test.com");
        anotherContractor.setUserRole(UserRole.CONTRACTOR);
        anotherContractor.setUserStatus(UserStatus.ACTIVE);
        anotherContractor.setAuth0UserId("auth0|contractor2-" + UUID.randomUUID());
        anotherContractor = usersRepository.save(anotherContractor);

        testLot.getAssignedUsers().add(anotherContractor);
        lotRepository.save(testLot);

        // Create a document uploaded by first contractor
        LotDocument doc = new LotDocument(
                testLot,
                contractorUser,
                contractorUser.getFirstName() + " " + contractorUser.getLastName(),
                "test-file.pdf",
                "storage-key-789",
                "application/pdf",
                1024L,
                false
        );
        doc = lotDocumentRepository.save(doc);
        final LotDocument savedDoc = doc;
        final Users finalAnotherContractor = anotherContractor;

        // Another contractor cannot delete first contractor's document
        assertThrows(AccessDeniedException.class, () -> {
                lotDocumentService.deleteDocument(
                    testLot.getLotIdentifier().getLotId().toString(),
                    savedDoc.getId(),
                    finalAnotherContractor.getUserIdentifier().getUserId().toString()
            );
        });
    }

    @Test
    public void testDeleteDocument_OutsiderCannotDelete() {
        // Create a document uploaded by contractor
        LotDocument doc = new LotDocument(
                testLot,
                contractorUser,
                contractorUser.getFirstName() + " " + contractorUser.getLastName(),
                "test-file.pdf",
                "storage-key-999",
                "application/pdf",
                1024L,
                false
        );
        doc = lotDocumentRepository.save(doc);
        final LotDocument savedDoc = doc;

        // Outsider cannot delete document
        assertThrows(AccessDeniedException.class, () -> {
                lotDocumentService.deleteDocument(
                    testLot.getLotIdentifier().getLotId().toString(),
                    savedDoc.getId(),
                    outsiderUser.getUserIdentifier().getUserId().toString()
            );
        });
    }

    @Test
    public void testDeleteDocument_DocumentNotInLot_ThrowsNotFoundException() {
        // Create another lot
        Lot anotherLot = new Lot(
            new LotIdentifier(UUID.randomUUID().toString()),
                "Another Lot",
                "456 Another St",
                150000f,
                "1500",
                "139.4",
                LotStatus.AVAILABLE
        );
        anotherLot.setProject(testProject);
        anotherLot = lotRepository.save(anotherLot);

        // Create document in testLot
        LotDocument doc = new LotDocument(
                testLot,
                contractorUser,
                contractorUser.getFirstName() + " " + contractorUser.getLastName(),
                "test-file.pdf",
                "storage-key-111",
                "application/pdf",
                1024L,
                false
        );
        doc = lotDocumentRepository.save(doc);
        final LotDocument savedDoc = doc;
        final Lot finalAnotherLot = anotherLot;

        // Try to delete from wrong lot
        assertThrows(NotFoundException.class, () -> {
                lotDocumentService.deleteDocument(
                    finalAnotherLot.getLotIdentifier().getLotId().toString(),
                    savedDoc.getId(),
                    ownerUser.getUserIdentifier().getUserId().toString()
            );
        });
    }
}
