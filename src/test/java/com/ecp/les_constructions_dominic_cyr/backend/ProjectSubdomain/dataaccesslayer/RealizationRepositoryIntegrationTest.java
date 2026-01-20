package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer;


import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.Realization;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Realization.RealizationRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository integration tests for Realization entity.
 * Uses @DataJpaTest which configures an in-memory H2 DB for tests by default.
 */
@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class RealizationRepositoryIntegrationTest {

    @Autowired
    private RealizationRepository realizationRepository;

}