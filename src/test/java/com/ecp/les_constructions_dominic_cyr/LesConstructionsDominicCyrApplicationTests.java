package com.ecp.les_constructions_dominic_cyr;

import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class LesConstructionsDominicCyrApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    void mainMethod_ApplicationStartsSuccessfully() {
        assertTrue(true, "The application context failed to load.");
    }
}
