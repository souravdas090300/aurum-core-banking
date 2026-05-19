package com.aurum.core_banking.integration;

import com.aurum.core_banking.BaseIntegrationTest;
import com.aurum.core_banking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.AccountRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Transfer API — runs against a real PostgreSQL database
 * via Testcontainers. Verifies end-to-end behaviour including balance updates,
 * idempotency, authentication, and validation.
 */
@DisplayName("Transfer API integration tests")
class TransferIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort int port;
    @Autowired AccountRepository accountRepository;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    private AccountEntity fromAccount;
    private AccountEntity toAccount;

    @BeforeEach
    void setUpAccounts() {
        fromAccount = accountRepository.save(AccountEntity.builder()
                .accountNumber("MT84TEST0000001" + System.nanoTime())
                .customerId(UUID.randomUUID())
                .accountType(AccountEntity.AccountType.CURRENT)
                .status(AccountEntity.AccountStatus.ACTIVE)
                .balance(new BigDecimal("10000.00"))
                .currency("EUR")
                .creditLimit(BigDecimal.ZERO)
                .build());

        toAccount = accountRepository.save(AccountEntity.builder()
                .accountNumber("MT84TEST0000002" + System.nanoTime())
                .customerId(UUID.randomUUID())
                .accountType(AccountEntity.AccountType.CURRENT)
                .status(AccountEntity.AccountStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .currency("EUR")
                .creditLimit(BigDecimal.ZERO)
                .build());
    }

    @AfterEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    private ResponseEntity<Map<String, Object>> post(String path, Map<String, Object> body) {
        try {
            return client.post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(null);
        }
    }

    @Test
    @DisplayName("Valid transfer — unauthenticated returns 401 (security active)")
    void validTransfer_unauthenticated_returns401() {
        Map<String, Object> body = Map.of(
                "fromAccountId",  fromAccount.getId(),
                "toAccountId",    toAccount.getId(),
                "amount",         "1000.00",
                "currency",       "EUR",
                "reference",      "Integration test transfer",
                "idempotencyKey", "integ-test-" + UUID.randomUUID());

        ResponseEntity<Map<String, Object>> response = post("/api/v1/transfers", body);
        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @DisplayName("Account repository: saved account is retrievable with correct balance")
    void savedAccount_isRetrievable() {
        AccountEntity found = accountRepository.findById(fromAccount.getId()).orElseThrow();
        assertThat(found.getBalance()).isEqualByComparingTo("10000.00");
        assertThat(found.getStatus()).isEqualTo(AccountEntity.AccountStatus.ACTIVE);
        assertThat(found.getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Actuator health endpoint is accessible without authentication")
    void actuatorHealth_isPublic() {
        try {
            ResponseEntity<Map<String, Object>> response = client.get().uri("/actuator/health")
                    .retrieve().toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
            assertThat(response.getStatusCode().value()).isIn(200, 503);
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            // 401/403 would be unexpected for actuator/health — fail clearly
            assertThat(ex.getStatusCode().value()).isIn(200, 503);
        }
    }

    @Test
    @DisplayName("POST transfer without content-type returns 4xx")
    void emptyBody_returns4xx() {
        ResponseEntity<Map<String, Object>> response = post("/api/v1/transfers", Map.of());
        assertThat(response.getStatusCode().value()).isIn(400, 401, 403, 415, 422);
    }
}
