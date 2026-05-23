package com.aurum.corebanking.api;

import com.aurum.corebanking.BaseIntegrationTest;
import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.corebanking.infrastructure.persistence.repository.AccountRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * REST-assured contract tests for the Transfer API.
 *
 * <p>These tests exercise the HTTP API through the full Spring Boot stack running
 * on a random port (via {@link BaseIntegrationTest}). They verify the JSON contract
 * and HTTP status codes returned to clients.
 *
 * <p>Authentication is bypassed in test mode (JWK URI is empty in application-test.yml);
 * tests include an Authorization header stub so the security filter chain allows access.
 */
@DisplayName("Transfer API contract tests (REST-assured)")
class TransferApiTest extends BaseIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    AccountRepository accountRepository;

    private AccountEntity fromAccount;
    private AccountEntity toAccount;

    @BeforeEach
    void setUp() {
        RestAssured.port    = port;
        RestAssured.baseURI = "http://localhost";

        fromAccount = accountRepository.save(AccountEntity.builder()
                .accountNumber("MT-API-001-" + System.nanoTime())
                .customerId(UUID.randomUUID())
                .accountType(AccountEntity.AccountType.CURRENT)
                .status(AccountEntity.AccountStatus.ACTIVE)
                .balance(new BigDecimal("8000.00"))
                .currency("EUR")
                .creditLimit(BigDecimal.ZERO)
                .build());

        toAccount = accountRepository.save(AccountEntity.builder()
                .accountNumber("MT-API-002-" + System.nanoTime())
                .customerId(UUID.randomUUID())
                .accountType(AccountEntity.AccountType.CURRENT)
                .status(AccountEntity.AccountStatus.ACTIVE)
                .balance(new BigDecimal("200.00"))
                .currency("EUR")
                .creditLimit(BigDecimal.ZERO)
                .build());
    }

    @AfterEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/transfers → 201 with correct response body")
    void transfer_returns201WithCorrectBody() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "fromAccountId",  fromAccount.getId(),
                "toAccountId",    toAccount.getId(),
                "amount",         "250.00",
                "currency",       "EUR",
                "reference",      "REST-assured test",
                "idempotencyKey", "ra-test-" + UUID.randomUUID()))
        .when()
            .post("/api/v1/transfers")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("status",      is("COMPLETED"))
            .body("amount",      is(250.0f))
            .body("currency",    is("EUR"))
            .body("transactionId", notNullValue());
    }

    @Test
    @DisplayName("POST /api/v1/transfers with missing body → 400 Bad Request")
    void transfer_missingBody_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/transfers")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/v1/transfers with negative amount → 400 Bad Request")
    void transfer_negativeAmount_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "fromAccountId",  fromAccount.getId(),
                "toAccountId",    toAccount.getId(),
                "amount",         "-100.00",
                "currency",       "EUR",
                "idempotencyKey", "ra-negative-" + UUID.randomUUID()))
        .when()
            .post("/api/v1/transfers")
        .then()
            .statusCode(400)
            .body("fields.amount", notNullValue());
    }

    @Test
    @DisplayName("POST /api/v1/transfers with insufficient funds → 422 with error detail")
    void transfer_insufficientFunds_returns422() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "fromAccountId",  fromAccount.getId(),
                "toAccountId",    toAccount.getId(),
                "amount",         "500000.00",
                "currency",       "EUR",
                "reference",      "Way too much",
                "idempotencyKey", "ra-insuf-" + UUID.randomUUID()))
        .when()
            .post("/api/v1/transfers")
        .then()
            .statusCode(422)
            .body("detail", containsString("Insufficient funds"));
    }

    @Test
    @DisplayName("POST /api/v1/transfers idempotent replay → 201 same transactionId")
    void transfer_idempotentReplay_returnsSameResponse() {
        String key = "ra-idem-" + UUID.randomUUID();
        Map<String, Object> body = Map.of(
                "fromAccountId",  fromAccount.getId(),
                "toAccountId",    toAccount.getId(),
                "amount",         "50.00",
                "currency",       "EUR",
                "reference",      "Idempotent test",
                "idempotencyKey", key);

        String txId1 = given().contentType(ContentType.JSON).body(body)
                .when().post("/api/v1/transfers")
                .then().statusCode(201)
                .extract().path("transactionId");

        String txId2 = given().contentType(ContentType.JSON).body(body)
                .when().post("/api/v1/transfers")
                .then().statusCode(201)
                .extract().path("transactionId");

        assertThat(txId1).isEqualTo(txId2);
    }
}
