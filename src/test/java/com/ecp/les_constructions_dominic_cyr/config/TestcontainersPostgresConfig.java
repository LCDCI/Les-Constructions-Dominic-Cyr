package com.ecp.les_constructions_dominic_cyr.config;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@TestConfiguration
public class TestcontainersPostgresConfig {

    @Bean
    @org.springframework.boot.testcontainers.service.connection.ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
    }
}