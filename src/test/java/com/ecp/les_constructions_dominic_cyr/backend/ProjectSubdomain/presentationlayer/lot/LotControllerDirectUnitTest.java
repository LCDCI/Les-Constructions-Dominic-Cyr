package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer.lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LotControllerDirectUnitTest {

    private LotService lotService;
    private UserService userService;
    private LotRepository lotRepository;
    private LotController lotController;

    @BeforeEach
    void setUp() {
        lotService = mock(LotService.class);
        userService = mock(UserService.class);
        lotRepository = mock(LotRepository.class);
        lotController = new LotController(lotService, userService, lotRepository);
    }

    @Test
    void getLotById_InvalidLength_ThrowsInvalidInput() {
        String shortId = "1234";
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> lotController.getLotById(shortId));
        assertTrue(ex.getMessage().contains("Invalid lot ID"));
    }

    @Test
    void getAllLotsByProject_NonOwner_FiltersByAssignedLots() {
        String projectId = "proj-x";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|u1");
        Authentication auth = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_CONTRACTOR"))).when(auth).getAuthorities();

        UserResponseModel userResp = new UserResponseModel();
        userResp.setUserIdentifier(UUID.randomUUID().toString());
        when(userService.getUserByAuth0Id("auth0|u1")).thenReturn(userResp);

        Lot lot = new Lot();
        Project proj = new Project();
        proj.setProjectIdentifier(projectId);
        when(lotRepository.findByAssignedUserId(any(UUID.class))).thenReturn(List.of(lot));
        when(lot.getProject()).thenReturn(proj);

        when(lotService.mapLotsToResponses(anyList())).thenReturn(List.of());

        var resp = lotController.getAllLotsByProject(projectId, null, jwt, auth);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void addLot_InvalidProjectIdentifier_ThrowsInvalidInput() {
        // pass blank project identifier should throw InvalidInputException
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> lotController.addLot("  ", null));
        assertTrue(ex.getMessage().contains("must not be blank"));
    }

    @Test
    void updateLot_InvalidId_ThrowsInvalidInput() {
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> lotController.updateLot(null, "short"));
        assertTrue(ex.getMessage().contains("Invalid lot ID"));
    }

    @Test
    void deleteLot_InvalidId_ThrowsInvalidInput() {
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> lotController.deleteLot("short"));
        assertTrue(ex.getMessage().contains("Invalid lot ID"));
    }
}
