package com.ecp.les_constructions_dominic_cyr.backend.config;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.Project;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project.ProjectRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.Realization;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederUnitTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RealizationRepository realizationRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private RenovationRepository renovationRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(projectRepository, realizationRepository, lotRepository, renovationRepository);
    }


    @Test
    void init_WhenRealizationExistsWithImage_DoesNotUpdate() {
        Realization realization = new Realization();
        realization.setRealizationIdentifier(new RealizationIdentifier("a3f1c0f1-8f2b-4c3d-9d5a-1b2a3c4d5e6f"));
        realization.setImageIdentifier("existing-image-id");

        when(projectRepository.findByProjectIdentifier(anyString())).thenReturn(Optional.empty());
        when(realizationRepository.findRealizationByRealizationIdentifier_RealizationId(anyString()))
                .thenReturn(realization);
        when(lotRepository.findByLotIdentifier_LotId(anyString())).thenReturn(null);
        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(anyString())).thenReturn(null);

        dataSeeder.init();

        verify(realizationRepository, never()).save(any(Realization.class));
    }

    @Test
    void init_WhenLotExistsWithImage_DoesNotUpdate() {
        Lot lot = new Lot();
        lot.setLotIdentifier(new LotIdentifier("f3c8837d-bd65-4bc5-9f01-cb9082fc657e"));

        when(projectRepository.findByProjectIdentifier(anyString())).thenReturn(Optional.empty());
        when(realizationRepository.findRealizationByRealizationIdentifier_RealizationId(anyString())).thenReturn(null);
        when(lotRepository.findByLotIdentifier_LotId(anyString()))
                .thenReturn(lot);
        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(anyString())).thenReturn(null);

        dataSeeder.init();

        verify(lotRepository, never()).save(any(Lot.class));
    }

    @Test
    void seedRenovationImages_WhenRenovationExistsAndImagesMissing_Saves() {
        Renovation renovation = new Renovation();
        renovation.setBeforeImageIdentifier(null);
        renovation.setAfterImageIdentifier("");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(anyString()))
                .thenReturn(renovation);
        when(renovationRepository.save(any(Renovation.class))).thenReturn(renovation);

        dataSeeder.seedRenovationImages();

        verify(renovationRepository, atLeastOnce()).save(any(Renovation.class));
    }

    @Test
    void seedRenovationImages_WhenRenovationExistsWithImages_DoesNotUpdate() {
        Renovation renovation = new Renovation();
        renovation.setBeforeImageIdentifier("existing-before");
        renovation.setAfterImageIdentifier("existing-after");

        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(anyString()))
                .thenReturn(renovation);

        dataSeeder.seedRenovationImages();

        verify(renovationRepository, never()).save(any(Renovation.class));
    }

    @Test
    void seedRenovationImages_WhenRenovationDoesNotExist_DoesNotSave() {
        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(anyString()))
                .thenReturn(null);

        dataSeeder.seedRenovationImages();

        verify(renovationRepository, never()).save(any(Renovation.class));
    }

    @Test
    void init_CallsAllSeedMethods() {
        // Mock all repositories to return empty/null to avoid actual processing
        when(projectRepository.findByProjectIdentifier(anyString())).thenReturn(Optional.empty());
        when(realizationRepository.findRealizationByRealizationIdentifier_RealizationId(anyString())).thenReturn(null);
        when(lotRepository.findByLotIdentifier_LotId(anyString())).thenReturn(null);
        when(renovationRepository.findRenovationByRenovationIdentifier_RenovationId(anyString())).thenReturn(null);

        dataSeeder.init();

        // Verify all seed methods are called
        verify(projectRepository, atLeastOnce()).findByProjectIdentifier(anyString());
        verify(realizationRepository, atLeastOnce()).findRealizationByRealizationIdentifier_RealizationId(anyString());
        verify(lotRepository, atLeastOnce()).findByLotIdentifier_LotId(anyString());
        verify(renovationRepository, atLeastOnce()).findRenovationByRenovationIdentifier_RenovationId(anyString());
    }
}