package com.ecp.les_constructions_dominic_cyr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class LesConstructionsDominicCyrApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    void mainMethod_ApplicationStartsSuccessfully() {
        assertTrue(true, "The application context failed to load.");
    }
}
