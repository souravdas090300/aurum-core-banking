package com.aurum.core_banking;

import com.aurum.core_banking.config.TestSecurityConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all integration tests.
 *
 * <p>Uses H2 in-memory database with PostgreSQL compatibility mode.
 * Database schema is created automatically via JPA's ddl-auto=create-drop.
 *
 * <p>Each test runs in isolation with a fresh H2 database instance.
 * No Docker/Testcontainers required - tests run fast and reliably on any platform.
 *
 * <p>Security is configured to allow all requests by default.
 * Use @WithMockUser on test methods to simulate authenticated users.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {
    // H2 database is configured in application-test.yml
    // Security is configured in TestSecurityConfig
}
