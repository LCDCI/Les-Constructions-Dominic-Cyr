package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.businesslayer.LotDocument;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument.LotDocument;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument.LotDocumentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument.LotDocumentResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotDocumentServiceImplUnitTest {

    @Mock private LotDocumentRepository lotDocumentRepository;
    @Mock private LotRepository lotRepository;
    @Mock private UsersRepository usersRepository;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private NotificationService notificationService;
    @Mock private MailerServiceClient mailerServiceClient;

    @InjectMocks
    private com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LotDocument.LotDocumentServiceImpl lotDocumentService;

    private UUID lotUuid;
    private Lot lot;
    private Users ownerUser;
    private Users contractorUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lotDocumentService, "filesServiceBaseUrl", "http://files-service");
        lotUuid = UUID.randomUUID();
        LotIdentifier lotId = new LotIdentifier(lotUuid.toString());
        Project project = new Project();
        project.setProjectIdentifier("proj-1");
        lot = new Lot();
        lot.setLotIdentifier(lotId);
        lot.setProject(project);

        UserIdentifier ownerId = UserIdentifier.newId();
        ownerUser = new Users();
        ownerUser.setUserIdentifier(ownerId);
        ownerUser.setUserRole(UserRole.OWNER);
        ownerUser.setFirstName("Owner");
        ownerUser.setLastName("User");
        ownerUser.setPrimaryEmail("owner@test.com");

        UserIdentifier contractorId = UserIdentifier.newId();
        contractorUser = new Users();
        contractorUser.setUserIdentifier(contractorId);
        contractorUser.setUserRole(UserRole.CONTRACTOR);
        contractorUser.setFirstName("Contractor");
        contractorUser.setLastName("User");
        lot.setAssignedUsers(List.of(contractorUser));
    }

    @Test
    void getLotDocuments_NoSearchOrType_ReturnsAllDocuments() {
        when(lotRepository.findByLotIdentifier_LotId(lotUuid)).thenReturn(lot);
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.findByLotId(lotUuid)).thenReturn(List.of(doc));

        List<LotDocumentResponseModel> result = lotDocumentService.getLotDocuments(lotUuid.toString(), null, null);

        assertEquals(1, result.size());
        assertEquals(doc.getId(), result.get(0).getId());
        verify(lotDocumentRepository).findByLotId(lotUuid);
    }

    @Test
    void getLotDocuments_WithSearch_CallsSearchByFileName() {
        when(lotRepository.findByLotIdentifier_LotId(lotUuid)).thenReturn(lot);
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.searchByLotIdAndFileName(lotUuid, "test")).thenReturn(List.of(doc));

        List<LotDocumentResponseModel> result = lotDocumentService.getLotDocuments(lotUuid.toString(), "test", null);

        assertEquals(1, result.size());
        verify(lotDocumentRepository).searchByLotIdAndFileName(lotUuid, "test");
    }

    @Test
    void getLotDocuments_TypeImage_CallsFindByLotIdAndType() {
        when(lotRepository.findByLotIdentifier_LotId(lotUuid)).thenReturn(lot);
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.findByLotIdAndType(lotUuid, true)).thenReturn(List.of(doc));

        List<LotDocumentResponseModel> result = lotDocumentService.getLotDocuments(lotUuid.toString(), null, "image");

        assertEquals(1, result.size());
        verify(lotDocumentRepository).findByLotIdAndType(lotUuid, true);
    }

    @Test
    void getLotDocuments_TypeFile_CallsFindByLotIdAndType() {
        when(lotRepository.findByLotIdentifier_LotId(lotUuid)).thenReturn(lot);
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.findByLotIdAndType(lotUuid, false)).thenReturn(List.of(doc));

        List<LotDocumentResponseModel> result = lotDocumentService.getLotDocuments(lotUuid.toString(), null, "file");

        assertEquals(1, result.size());
        verify(lotDocumentRepository).findByLotIdAndType(lotUuid, false);
    }

    @Test
    void getLotDocuments_LotNotFound_ThrowsNotFoundException() {
        when(lotRepository.findByLotIdentifier_LotId(lotUuid)).thenReturn(null);

        assertThrows(NotFoundException.class, () ->
                lotDocumentService.getLotDocuments(lotUuid.toString(), null, null));
    }

    @Test
    void uploadDocuments_NoFiles_ThrowsInvalidInputException() {
        assertThrows(InvalidInputException.class, () ->
                lotDocumentService.uploadDocuments(lotUuid.toString(), null, ownerUser.getUserIdentifier().getUserId().toString()));
        assertThrows(InvalidInputException.class, () ->
                lotDocumentService.uploadDocuments(lotUuid.toString(), new org.springframework.web.multipart.MultipartFile[0], ownerUser.getUserIdentifier().getUserId().toString()));
    }

    @Test
    void downloadDocument_DocumentNotInLot_ThrowsNotFoundException() {
        UUID docId = UUID.randomUUID();
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                lotDocumentService.downloadDocument(lotUuid.toString(), docId, ownerUser.getUserIdentifier().getUserId().toString()));
    }

    @Test
    void downloadDocument_RequesterNotFound_ThrowsNotFoundException() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(usersRepository.findByUserIdentifier_UserId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                lotDocumentService.downloadDocument(lotUuid.toString(), docId, UUID.randomUUID().toString()));
    }

    @Test
    void downloadDocument_NonOwnerNotAssigned_ThrowsAccessDeniedException() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        Users unassigned = new Users();
        unassigned.setUserIdentifier(UserIdentifier.newId());
        unassigned.setUserRole(UserRole.CUSTOMER);
        Lot lotNoUser = new Lot();
        lotNoUser.setAssignedUsers(List.of());
        doc.setLot(lotNoUser);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(usersRepository.findByUserIdentifier_UserId(unassigned.getUserIdentifier().getUserId())).thenReturn(Optional.of(unassigned));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                lotDocumentService.downloadDocument(lotUuid.toString(), docId, unassigned.getUserIdentifier().getUserId().toString()));
    }

    @Test
    void downloadDocument_Success_ReturnsBytes() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        doc.setStorageKey("key1");
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(usersRepository.findByUserIdentifier_UserId(ownerUser.getUserIdentifier().getUserId())).thenReturn(Optional.of(ownerUser));

        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(getSpec);
        when(getSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(eq(byte[].class))).thenReturn(Mono.just(new byte[]{1, 2, 3}));

        byte[] result = lotDocumentService.downloadDocument(lotUuid.toString(), docId, ownerUser.getUserIdentifier().getUserId().toString());

        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    void deleteDocument_NonOwnerNotUploader_ThrowsAccessDeniedException() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        doc.setUploader(contractorUser);
        lot.setAssignedUsers(List.of(ownerUser, contractorUser));
        doc.setLot(lot);
        Users otherUser = new Users();
        otherUser.setUserIdentifier(UserIdentifier.newId());
        otherUser.setUserRole(UserRole.CUSTOMER);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(usersRepository.findByUserIdentifier_UserId(otherUser.getUserIdentifier().getUserId())).thenReturn(Optional.of(otherUser));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                lotDocumentService.deleteDocument(lotUuid.toString(), docId, otherUser.getUserIdentifier().getUserId().toString()));
    }

    @Test
    void downloadDocument_InvalidRequestingUserId_ThrowsInvalidInputException() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));

        assertThrows(InvalidInputException.class, () ->
                lotDocumentService.downloadDocument(lotUuid.toString(), docId, "not-a-uuid"));
    }

    @Test
    void deleteDocument_DocumentNotInLot_ThrowsNotFoundException() {
        UUID docId = UUID.randomUUID();
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                lotDocumentService.deleteDocument(lotUuid.toString(), docId, ownerUser.getUserIdentifier().getUserId().toString()));
    }

    @Test
    void deleteDocument_OwnerCanDelete_DeletesDocument() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        doc.setUploader(contractorUser);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(usersRepository.findByUserIdentifier_UserId(ownerUser.getUserIdentifier().getUserId())).thenReturn(Optional.of(ownerUser));

        lotDocumentService.deleteDocument(lotUuid.toString(), docId, ownerUser.getUserIdentifier().getUserId().toString());

        verify(lotDocumentRepository).delete(doc);
    }

    @Test
    void getDocumentContentType_DocumentNotFound_ThrowsNotFoundException() {
        UUID docId = UUID.randomUUID();
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                lotDocumentService.getDocumentContentType(lotUuid.toString(), docId));
    }

    @Test
    void getDocumentContentType_Success_ReturnsMimeType() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        doc.setMimeType("application/pdf");
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));

        String result = lotDocumentService.getDocumentContentType(lotUuid.toString(), docId);

        assertEquals("application/pdf", result);
    }

    @Test
    void uploadDocuments_OwnerSuccess_ReturnsSavedDocuments() {
        when(lotRepository.findByLotIdentifier_LotId(any(UUID.class))).thenReturn(lot);
        when(usersRepository.findByUserIdentifier_UserId(eq(ownerUser.getUserIdentifier().getUserId()))).thenReturn(Optional.of(ownerUser));
        mockWebClientPostResponse("{\"fileId\": \"storage-key-123\"}");

        LotDocument savedDoc = createDoc(lotUuid);
        when(lotDocumentRepository.save(any(LotDocument.class))).thenAnswer(inv -> {
            LotDocument d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", UUID.randomUUID());
            return d;
        });

        List<LotDocumentResponseModel> result = lotDocumentService.uploadDocuments(
                lotUuid.toString(),
                new org.springframework.web.multipart.MultipartFile[]{mockFile()},
                ownerUser.getUserIdentifier().getUserId().toString());

        assertEquals(1, result.size());
        verify(lotDocumentRepository).save(any(LotDocument.class));
        verify(notificationService, atLeast(0)).createNotification(any(), any(), any(), any(), any());
    }


    @Test
    void uploadDocuments_LotHasNoProject_StillSaves_SkipsNotification() {
        lot.setProject(null);
        when(lotRepository.findByLotIdentifier_LotId(any(UUID.class))).thenReturn(lot);
        when(usersRepository.findByUserIdentifier_UserId(eq(ownerUser.getUserIdentifier().getUserId()))).thenReturn(Optional.of(ownerUser));
        mockWebClientPostResponse("{\"fileId\": \"key-no-proj\"}");
        when(lotDocumentRepository.save(any(LotDocument.class))).thenAnswer(inv -> {
            LotDocument d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", UUID.randomUUID());
            return d;
        });

        List<LotDocumentResponseModel> result = lotDocumentService.uploadDocuments(
                lotUuid.toString(),
                new org.springframework.web.multipart.MultipartFile[]{mockFile()},
                ownerUser.getUserIdentifier().getUserId().toString());

        assertEquals(1, result.size());
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    void uploadDocuments_ContractorSuccess_WhenAssignedToLot() {
        when(lotRepository.findByLotIdentifier_LotId(any(UUID.class))).thenReturn(lot);
        when(usersRepository.findByUserIdentifier_UserId(eq(contractorUser.getUserIdentifier().getUserId()))).thenReturn(Optional.of(contractorUser));
        mockWebClientPostResponse("{\"fileId\": \"key-contractor\"}");
        when(lotDocumentRepository.save(any(LotDocument.class))).thenAnswer(inv -> {
            LotDocument d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", UUID.randomUUID());
            return d;
        });

        List<LotDocumentResponseModel> result = lotDocumentService.uploadDocuments(
                lotUuid.toString(),
                new org.springframework.web.multipart.MultipartFile[]{mockFile()},
                contractorUser.getUserIdentifier().getUserId().toString());

        assertEquals(1, result.size());
        verify(lotDocumentRepository).save(any(LotDocument.class));
    }

    @Test
    void uploadDocuments_FileUploadFails_ThrowsRuntimeException() {
        when(lotRepository.findByLotIdentifier_LotId(any(UUID.class))).thenReturn(lot);
        when(usersRepository.findByUserIdentifier_UserId(eq(ownerUser.getUserIdentifier().getUserId()))).thenReturn(Optional.of(ownerUser));
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        WebClient webClient = mock(WebClient.class);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenThrow(new RuntimeException("Network error"));

        assertThrows(RuntimeException.class, () ->
                lotDocumentService.uploadDocuments(
                        lotUuid.toString(),
                        new org.springframework.web.multipart.MultipartFile[]{mockFile()},
                        ownerUser.getUserIdentifier().getUserId().toString()));
    }

    @Test
    void uploadDocuments_WithImageMimeType_SetsIsImageTrue() {
        when(lotRepository.findByLotIdentifier_LotId(any(UUID.class))).thenReturn(lot);
        when(usersRepository.findByUserIdentifier_UserId(eq(ownerUser.getUserIdentifier().getUserId()))).thenReturn(Optional.of(ownerUser));
        mockWebClientPostResponse("{\"fileId\": \"img-key\"}");
        ArgumentCaptor<LotDocument> docCaptor = ArgumentCaptor.forClass(LotDocument.class);
        when(lotDocumentRepository.save(docCaptor.capture())).thenAnswer(inv -> {
            LotDocument d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", UUID.randomUUID());
            return d;
        });

        org.springframework.web.multipart.MultipartFile imageFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(imageFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getSize()).thenReturn(5000L);
        try {
            when(imageFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        } catch (Exception ignored) {}

        lotDocumentService.uploadDocuments(
                lotUuid.toString(),
                new org.springframework.web.multipart.MultipartFile[]{imageFile},
                ownerUser.getUserIdentifier().getUserId().toString());

        LotDocument saved = docCaptor.getValue();
        assertTrue(saved.getIsImage());
    }

    @Test
    void uploadDocuments_ResponseWithIdKey_ExtractsStorageKey() {
        when(lotRepository.findByLotIdentifier_LotId(any(UUID.class))).thenReturn(lot);
        when(usersRepository.findByUserIdentifier_UserId(eq(ownerUser.getUserIdentifier().getUserId()))).thenReturn(Optional.of(ownerUser));
        mockWebClientPostResponse("{\"id\": \"extracted-id-value\"}");
        ArgumentCaptor<LotDocument> docCaptor = ArgumentCaptor.forClass(LotDocument.class);
        when(lotDocumentRepository.save(docCaptor.capture())).thenAnswer(inv -> {
            LotDocument d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", UUID.randomUUID());
            return d;
        });

        lotDocumentService.uploadDocuments(
                lotUuid.toString(),
                new org.springframework.web.multipart.MultipartFile[]{mockFile()},
                ownerUser.getUserIdentifier().getUserId().toString());

        assertEquals("extracted-id-value", docCaptor.getValue().getStorageKey());
    }

    @Test
    void uploadDocuments_NullContentType_DefaultsToOctetStream() {
        when(lotRepository.findByLotIdentifier_LotId(any(UUID.class))).thenReturn(lot);
        when(usersRepository.findByUserIdentifier_UserId(eq(ownerUser.getUserIdentifier().getUserId()))).thenReturn(Optional.of(ownerUser));
        mockWebClientPostResponse("{\"fileId\": \"key1\"}");
        ArgumentCaptor<LotDocument> docCaptor = ArgumentCaptor.forClass(LotDocument.class);
        when(lotDocumentRepository.save(docCaptor.capture())).thenAnswer(inv -> {
            LotDocument d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", UUID.randomUUID());
            return d;
        });

        org.springframework.web.multipart.MultipartFile fileNoContentType = mock(org.springframework.web.multipart.MultipartFile.class);
        when(fileNoContentType.getOriginalFilename()).thenReturn("file.bin");
        when(fileNoContentType.getContentType()).thenReturn(null);
        when(fileNoContentType.getSize()).thenReturn(100L);
        try {
            when(fileNoContentType.getBytes()).thenReturn(new byte[]{1});
        } catch (Exception ignored) {}

        lotDocumentService.uploadDocuments(
                lotUuid.toString(),
                new org.springframework.web.multipart.MultipartFile[]{fileNoContentType},
                ownerUser.getUserIdentifier().getUserId().toString());

        assertEquals("application/octet-stream", docCaptor.getValue().getMimeType());
    }

    @Test
    void deleteDocument_RequesterNotFound_ThrowsNotFoundException() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(usersRepository.findByUserIdentifier_UserId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                lotDocumentService.deleteDocument(lotUuid.toString(), docId, UUID.randomUUID().toString()));
    }

    @Test
    void deleteDocument_InvalidRequestingUserId_ThrowsInvalidInputException() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));

        assertThrows(InvalidInputException.class, () ->
                lotDocumentService.deleteDocument(lotUuid.toString(), docId, "not-a-uuid"));
    }

    @Test
    void deleteDocument_ContractorAsUploader_CanDelete() {
        UUID docId = UUID.randomUUID();
        LotDocument doc = createDoc(lotUuid);
        doc.setUploader(contractorUser);
        doc.setLot(lot);
        when(lotDocumentRepository.existsByIdAndLotId(docId, lotUuid)).thenReturn(true);
        when(lotDocumentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(usersRepository.findByUserIdentifier_UserId(any(UUID.class))).thenReturn(Optional.of(contractorUser));

        lotDocumentService.deleteDocument(lotUuid.toString(), docId, contractorUser.getUserIdentifier().getUserId().toString());

        verify(lotDocumentRepository).delete(doc);
    }

    @Test
    void getLotDocuments_BlankSearch_ReturnsAllDocuments() {
        when(lotRepository.findByLotIdentifier_LotId(lotUuid)).thenReturn(lot);
        LotDocument doc = createDoc(lotUuid);
        when(lotDocumentRepository.findByLotId(lotUuid)).thenReturn(List.of(doc));

        List<LotDocumentResponseModel> result = lotDocumentService.getLotDocuments(lotUuid.toString(), "   ", null);

        assertEquals(1, result.size());
        verify(lotDocumentRepository).findByLotId(lotUuid);
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientPostResponse(String responseJson) {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(bodySpec);
        doReturn(bodySpec).when(bodySpec).body(any());
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));
    }

    private LotDocument createDoc(UUID lotId) {
        LotIdentifier li = new LotIdentifier(lotId.toString());
        Lot l = new Lot();
        l.setLotIdentifier(li);
        LotDocument doc = new LotDocument(l, ownerUser, "Uploader", "file.pdf", "key", "application/pdf", 100L, false);
        org.springframework.test.util.ReflectionTestUtils.setField(doc, "id", UUID.randomUUID());
        return doc;
    }

    private org.springframework.web.multipart.MultipartFile mockFile() {
        org.springframework.web.multipart.MultipartFile f = mock(org.springframework.web.multipart.MultipartFile.class);
        when(f.getOriginalFilename()).thenReturn("test.pdf");
        when(f.getContentType()).thenReturn(MediaType.APPLICATION_PDF_VALUE);
        when(f.getSize()).thenReturn(100L);
        try {
            when(f.getBytes()).thenReturn(new byte[]{1, 2, 3});
        } catch (Exception ignored) {}
        return f;
    }
}
