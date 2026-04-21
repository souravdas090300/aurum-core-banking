package com.aurum.core_banking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests that verify the security filter chain behaves correctly
 * without starting a real Keycloak instance.
 *
 * <p>These tests use {@code MockMvc} against the full application context so
 * Spring Security applies exactly as it would in production.
 */
@SpringBootTest
class SecurityIntegrationTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void transfers_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/transfers")
                .contentType("application/json")
                .content("{}"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void accounts_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/00000000-0000-0000-0000-000000000001"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealth_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isOk());
    }

    @Test
    void actuatorMetrics_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
               .andExpect(status().isUnauthorized());
    }
}
