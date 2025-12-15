package com.ecp.les_constructions_dominic_cyr.config;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.Renovation;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.RenovationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.DataSeeder;
import jakarta.transaction.Transactional;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@Transactional
public class DataSeederIntegrationTest {

    @Autowired
    DataSeeder dataSeeder;

    @Autowired
    RenovationRepository renovationRepository;

    @Test
    public void linksImagesInDatabase() {
        Renovation renovation = new Renovation();
        renovationRepository.save(renovation);

        dataSeeder.seedRenovationImages();

        Renovation persisted =
                renovationRepository.findById(renovation.getId()).orElseThrow();

        assertNotNull(persisted.getBeforeImageIdentifier());
        assertNotNull(persisted.getAfterImageIdentifier());
    }
}

