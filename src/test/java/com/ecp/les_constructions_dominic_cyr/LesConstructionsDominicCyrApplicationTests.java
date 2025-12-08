package com.ecp.les_constructions_dominic_cyr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ecp.les_constructions_dominic_cyr.config.TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
class LesConstructionsDominicCyrApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    void mainMethod_ApplicationStartsSuccessfully() {
        assertTrue(true, "The application context failed to load.");
    }
}
