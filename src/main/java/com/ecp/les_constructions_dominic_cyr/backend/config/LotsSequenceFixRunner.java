package com.ecp.les_constructions_dominic_cyr.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs once after the application context is up and syncs the PostgreSQL
 * sequence for {@code lots.id} with the current max ID. This avoids duplicate-key
 * errors when inserting new lots if seed data (or manual inserts) used explicit IDs.
 * Safe to run on every startup (idempotent).
 */
@Component
@Order(Integer.MAX_VALUE) // Run after other startup logic
public class LotsSequenceFixRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LotsSequenceFixRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public LotsSequenceFixRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                "SELECT setval('lots_id_seq', (SELECT COALESCE(MAX(id), 0) FROM lots))"
            );
            log.debug("Lots sequence synced to current max id");
        } catch (Exception e) {
            log.warn("Could not sync lots_id_seq (non-fatal): {}", e.getMessage());
        }
    }
}
