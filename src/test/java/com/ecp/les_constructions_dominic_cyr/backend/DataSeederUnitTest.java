package com.ecp.les_constructions_dominic_cyr.backend.config;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederUnitTest {

    @Mock
    RenovationRepository renovationRepository;

    @InjectMocks
    DataSeeder dataSeeder;

    @Test
    void savesRenovationWhenBeforeAndAfterImagesMissing() {
        Renovation renovation = new Renovation();
        renovation.setBeforeImageIdentifier(null);
        renovation.setAfterImageIdentifier("");

        when(renovationRepository
                .findRenovationByRenovationIdentifier_RenovationId(any()))
                .thenReturn(renovation);

        dataSeeder.seedRenovationImages();

        verify(renovationRepository).save(renovation);
    }
}

