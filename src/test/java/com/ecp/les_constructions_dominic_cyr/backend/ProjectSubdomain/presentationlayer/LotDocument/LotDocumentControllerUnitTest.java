package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer.LotDocument;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LotDocument.LotDocumentService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument.LotDocumentController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument.LotDocumentResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotDocumentControllerUnitTest {

    private LotDocumentService lotDocumentService;
    private UserService userService;
    private LotRepository lotRepository;
    private LotDocumentController controller;

    private String lotId;
    private Jwt jwt;
    private Authentication ownerAuth;
    private UserResponseModel userResponse;

    @BeforeEach
    void setUp() {
        lotDocumentService = mock(LotDocumentService.class);
        userService = mock(UserService.class);
        lotRepository = mock(LotRepository.class);
        controller = new LotDocumentController(lotDocumentService, userService, lotRepository);

        lotId = UUID.randomUUID().toString();
        jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|user1");
        ownerAuth = mock(Authentication.class);
        doReturn(List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_OWNER"))).when(ownerAuth).getAuthorities();
        userResponse = new UserResponseModel();
        userResponse.setUserIdentifier(UUID.randomUUID().toString());
    }

    @Test
    void getLotDocuments_WhenAuthorized_ReturnsOk() {
        List<LotDocumentResponseModel> docs = List.of(
            LotDocumentResponseModel.builder().id(UUID.randomUUID()).fileName("doc.pdf").build()
        );
        when(lotDocumentService.getLotDocuments(eq(lotId), any(), any())).thenReturn(docs);

        ResponseEntity<List<LotDocumentResponseModel>> resp = controller.getLotDocuments(
            lotId, null, "all", jwt, ownerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        assertEquals("doc.pdf", resp.getBody().get(0).getFileName());
    }

    @Test
    void getLotDocuments_WhenUnauthorized_ThrowsAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () ->
            controller.getLotDocuments(lotId, null, "all", null, null));
    }

    @Test
    void uploadDocuments_WhenNoFiles_ThrowsInvalidInputException() {
        when(userService.getUserByAuth0Id(anyString())).thenReturn(userResponse);
        when(lotRepository.findByAssignedUserId(any(UUID.class))).thenReturn(List.of(mock(Lot.class)));

        assertThrows(com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException.class, () ->
            controller.uploadDocuments(lotId, null, jwt, ownerAuth));
    }

    @Test
    void downloadDocument_WhenAuthorized_ReturnsResource() {
        when(userService.getUserByAuth0Id(anyString())).thenReturn(userResponse);
        when(lotRepository.findByAssignedUserId(any(UUID.class))).thenReturn(List.of(mock(Lot.class)));
        UUID docId = UUID.randomUUID();
        when(lotDocumentService.downloadDocument(eq(lotId), eq(docId), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(lotDocumentService.getDocumentContentType(eq(lotId), eq(docId))).thenReturn("application/pdf");

        ResponseEntity<org.springframework.core.io.Resource> resp = controller.downloadDocument(
            lotId, docId, jwt, ownerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void deleteDocument_WhenAuthorized_ReturnsOk() {
        when(userService.getUserByAuth0Id(anyString())).thenReturn(userResponse);
        when(lotRepository.findByAssignedUserId(any(UUID.class))).thenReturn(List.of(mock(Lot.class)));
        UUID docId = UUID.randomUUID();

        ResponseEntity<?> resp = controller.deleteDocument(lotId, docId, jwt, ownerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Document deleted successfully", ((java.util.Map<?, ?>) resp.getBody()).get("message"));
        verify(lotDocumentService).deleteDocument(eq(lotId), eq(docId), anyString());
    }

    @Test
    void validateLotAccess_NonOwnerNotAssigned_ThrowsAccessDeniedException() {
        Authentication nonOwnerAuth = mock(Authentication.class);
        doReturn(List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_CONTRACTOR"))).when(nonOwnerAuth).getAuthorities();
        when(userService.getUserByAuth0Id(anyString())).thenReturn(userResponse);
        when(lotRepository.findByAssignedUserId(any(UUID.class))).thenReturn(List.of()); // not assigned

        assertThrows(AccessDeniedException.class, () ->
            controller.getLotDocuments(lotId, null, "all", jwt, nonOwnerAuth));
    }
}
