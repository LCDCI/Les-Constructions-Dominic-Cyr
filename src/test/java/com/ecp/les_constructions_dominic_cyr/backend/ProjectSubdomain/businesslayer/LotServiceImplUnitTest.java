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
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LotServiceImplUnitTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private LotServiceImpl lotService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setProjectIdentifier("proj-123");
        testProject.setProjectName("Test Project");
    }

    @Test
    void getLotById_WhenNotFound_ThrowsNotFound() {
        when(lotRepository.findByLotIdentifier_LotId(eq("non-existent"))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> lotService.getLotById("non-existent"));

        verify(lotRepository, times(1)).findByLotIdentifier_LotId(eq("non-existent"));
    }

    @Test
    void addLotToProject_WithAssignedUsers_SetsReservedStatus() {
        LotRequestModel req = new LotRequestModel();
        req.setLotNumber("L-1");
        req.setCivicAddress("123 Main St");
        req.setPrice(100000f);
        req.setDimensionsSquareFeet("1000");
        req.setDimensionsSquareMeters("93");
        req.setLotStatus(LotStatus.AVAILABLE);
        req.setAssignedUserIds(List.of(UUID.randomUUID().toString()));

        when(projectRepository.findByProjectIdentifier(eq("proj-123"))).thenReturn(Optional.of(testProject));
        when(usersRepository.findByUserIdentifier_UserId(any(UUID.class))).thenReturn(Optional.of(new Users()));

        Lot saved = new Lot(new LotIdentifier(), "L-1", "123 Main St", 100000f, "1000", "93", LotStatus.RESERVED);
        when(lotRepository.save(any(Lot.class))).thenReturn(saved);

        LotResponseModel result = lotService.addLotToProject("proj-123", req);

        assertNotNull(result);
        assertEquals(LotStatus.RESERVED, result.getLotStatus());
        verify(projectRepository, times(1)).findByProjectIdentifier(eq("proj-123"));
        verify(lotRepository, times(1)).save(any(Lot.class));
    }

    @Test
    void mapToResponse_CalculatesProgressPercentage() {
        Lot lot = new Lot(new LotIdentifier(), "L-1", "Addr", 0f, "100", "9", LotStatus.AVAILABLE);
        lot.setRemainingUpcomingWork(30);

        when(lotRepository.findAll()).thenReturn(List.of(lot));

        List<LotResponseModel> responses = lotService.mapLotsToResponses(List.of(lot));
        assertEquals(1, responses.size());
        assertNotNull(responses.get(0).getProgressPercentage());
        assertTrue(responses.get(0).getProgressPercentage() >= 0 && responses.get(0).getProgressPercentage() <= 100);
    }
}
