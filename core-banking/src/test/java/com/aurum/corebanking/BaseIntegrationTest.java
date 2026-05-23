package com.aurum.corebanking;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests.
 *
 * <p>Spins up a shared PostgreSQL 16 Testcontainer with container reuse enabled —
 * the same container is reused across all test classes in a single Surefire JVM,
 * dramatically reducing total test time.
 *
 * <p>Flyway migrations run automatically on the shared container DB; each test that
 * modifies data must clean up after itself (or use @Transactional rollback).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    @SuppressWarnings("resource") // Managed by Testcontainers framework
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("aurum_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    @BeforeAll
    static void startContainer() {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
