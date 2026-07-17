package com.aurum.corebanking;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

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
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("aurum_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    @BeforeAll
    static void startContainer() {
        try {
            POSTGRES.start();
        } catch (Exception e) {
            // Docker not available - skip all integration tests
            Assumptions.assumeTrue(false, "Docker environment not available - skipping integration tests");
        }
    }

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
