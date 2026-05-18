package com.aurum.core_banking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring context smoke test — verifies the application context loads successfully.
 *
 * <p>Uses the test profile which disables Keycloak JWT validation and
 * points the data source to the Testcontainers PostgreSQL instance via
 * {@link BaseIntegrationTest}.
 */
class CoreBankingApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
        // If the Spring context starts without exceptions, this test passes.
    }
}
