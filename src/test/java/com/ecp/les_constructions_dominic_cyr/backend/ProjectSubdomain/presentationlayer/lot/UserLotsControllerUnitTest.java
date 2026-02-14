package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer.lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.UserLotsController;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserLotsControllerUnitTest {

    private LotService lotService;
    private UserService userService;
    private LotRepository lotRepository;
    private UserLotsController controller;

    private Jwt jwt;
    private Authentication ownerAuth;
    private Authentication nonOwnerAuth;
    private UserResponseModel userResponse;

    @BeforeEach
    void setUp() {
        lotService = mock(LotService.class);
        userService = mock(UserService.class);
        lotRepository = mock(LotRepository.class);
        controller = new UserLotsController(lotService, userService, lotRepository);

        jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|user1");
        ownerAuth = mock(Authentication.class);
        doReturn(List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_OWNER"))).when(ownerAuth).getAuthorities();
        nonOwnerAuth = mock(Authentication.class);
        doReturn(List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_CONTRACTOR"))).when(nonOwnerAuth).getAuthorities();
        userResponse = new UserResponseModel();
        userResponse.setUserIdentifier(UUID.randomUUID().toString());
    }

    @Test
    void getUserLots_Unauthenticated_Returns401() {
        ResponseEntity<List<LotResponseModel>> resp = controller.getUserLots(null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void getUserLots_AsOwner_ReturnsAllLots() {
        Lot lot = new Lot();
        lot.setLotIdentifier(new LotIdentifier(UUID.randomUUID().toString()));
        LotResponseModel model = new LotResponseModel();
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(lotService.mapLotsToResponses(anyList())).thenReturn(List.of(model));

        ResponseEntity<List<LotResponseModel>> resp = controller.getUserLots(jwt, ownerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        verify(lotRepository).findAll();
    }

    @Test
    void getUserLots_AsNonOwner_ReturnsAssignedLotsOnly() {
        when(userService.getUserByAuth0Id("auth0|user1")).thenReturn(userResponse);
        Lot lot = new Lot();
        lot.setLotIdentifier(new LotIdentifier(userResponse.getUserIdentifier()));
        when(lotRepository.findByAssignedUserId(UUID.fromString(userResponse.getUserIdentifier()))).thenReturn(List.of(lot));
        LotResponseModel model = new LotResponseModel();
        when(lotService.mapLotsToResponses(anyList())).thenReturn(List.of(model));

        ResponseEntity<List<LotResponseModel>> resp = controller.getUserLots(jwt, nonOwnerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void getUserLots_NonOwnerUserNotFound_Returns401() {
        when(userService.getUserByAuth0Id(anyString())).thenThrow(new RuntimeException("not found"));

        ResponseEntity<List<LotResponseModel>> resp = controller.getUserLots(jwt, nonOwnerAuth);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void getUserLots_NonOwnerInvalidUserIdentifier_ReturnsEmptyList() {
        userResponse.setUserIdentifier("not-a-uuid");
        when(userService.getUserByAuth0Id("auth0|user1")).thenReturn(userResponse);

        ResponseEntity<List<LotResponseModel>> resp = controller.getUserLots(jwt, nonOwnerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    void getLotById_Unauthenticated_Returns401() {
        ResponseEntity<LotResponseModel> resp = controller.getLotById(UUID.randomUUID().toString(), null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void getLotById_AsOwner_ReturnsLot() {
        String lotId = UUID.randomUUID().toString();
        LotResponseModel model = new LotResponseModel();
        when(lotService.getLotById(lotId)).thenReturn(model);

        ResponseEntity<LotResponseModel> resp = controller.getLotById(lotId, jwt, ownerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(model, resp.getBody());
    }

    @Test
    void getLotById_NonOwnerWithAccess_ReturnsLot() {
        String lotId = UUID.randomUUID().toString();
        LotResponseModel model = new LotResponseModel();
        when(lotService.getLotById(lotId)).thenReturn(model);
        when(userService.getUserByAuth0Id("auth0|user1")).thenReturn(userResponse);
        Lot lot = new Lot();
        lot.setLotIdentifier(new LotIdentifier(lotId));
        when(lotRepository.findByAssignedUserId(UUID.fromString(userResponse.getUserIdentifier()))).thenReturn(List.of(lot));

        ResponseEntity<LotResponseModel> resp = controller.getLotById(lotId, jwt, nonOwnerAuth);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(model, resp.getBody());
    }

    @Test
    void getLotById_NonOwnerWithoutAccess_Returns403() {
        String lotId = UUID.randomUUID().toString();
        LotResponseModel model = new LotResponseModel();
        when(lotService.getLotById(lotId)).thenReturn(model);
        when(userService.getUserByAuth0Id("auth0|user1")).thenReturn(userResponse);
        when(lotRepository.findByAssignedUserId(UUID.fromString(userResponse.getUserIdentifier()))).thenReturn(List.of());

        ResponseEntity<LotResponseModel> resp = controller.getLotById(lotId, jwt, nonOwnerAuth);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void getLotById_InvalidLotId_Returns400() {
        when(lotService.getLotById("not-a-uuid")).thenReturn(new LotResponseModel());
        when(userService.getUserByAuth0Id("auth0|user1")).thenReturn(userResponse);
        ResponseEntity<LotResponseModel> resp = controller.getLotById("not-a-uuid", jwt, nonOwnerAuth);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}
