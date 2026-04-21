package com.aurum.core_banking.rules;

import com.aurum.core_banking.domain.rules.TransactionFact;
import com.aurum.core_banking.infrastructure.rules.DroolsConfig;
import org.kie.api.runtime.KieContainer;
import org.junit.jupiter.api.*;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DroolsConfig.class)
class FraudDetectionRulesTest {

    @Autowired KieContainer kieContainer;
    KieSession session;

    @BeforeEach
    void setUp() { session = kieContainer.newKieSession(); }

    @AfterEach
    void tearDown() { session.dispose(); }

    @Test
    @DisplayName("Transaction ≥ €10,000 triggers FIAU report flag")
    void largeTransaction_shouldSetFiauFlag() {
        TransactionFact tx = TransactionFact.builder()
            .transactionId(UUID.randomUUID())
            .amount(new BigDecimal("10000.00"))
            .fromCountry("MT")
            .toCountry("DE")
            .transactionsLastHour(1)
            .isOffHours(false)
            .build();

        session.insert(tx);
        session.fireAllRules();

        assertThat(tx.isFiauReport()).isTrue();
        assertThat(tx.getFraudFlags()).contains("FIAU_REPORT_REQUIRED");
        assertThat(tx.isBlocked()).isFalse(); // should flag, not block
    }

    @Test
    @DisplayName("Velocity breach blocks transaction")
    void velocityBreach_shouldBlockTransaction() {
        TransactionFact tx = TransactionFact.builder()
            .transactionId(UUID.randomUUID())
            .amount(new BigDecimal("500.00"))
            .transactionsLastHour(8) // over threshold of 5
            .fromCountry("MT")
            .toCountry("MT")
            .isOffHours(false)
            .build();

        session.insert(tx);
        session.fireAllRules();

        assertThat(tx.isBlocked()).isTrue();
        assertThat(tx.getFraudFlags()).contains("STRUCTURING_SUSPECTED");
        assertThat(tx.getBlockReason()).contains("Velocity breach");
    }

    @Test
    @DisplayName("Sanctioned country transfer is immediately blocked")
    void sanctionedCountry_shouldBeBlocked() {
        TransactionFact tx = TransactionFact.builder()
            .transactionId(UUID.randomUUID())
            .amount(new BigDecimal("100.00"))
            .fromCountry("MT")
            .toCountry("KP") // North Korea — sanctioned
            .transactionsLastHour(1)
            .isOffHours(false)
            .build();

        session.insert(tx);
        session.fireAllRules();

        assertThat(tx.isBlocked()).isTrue();
        assertThat(tx.getFraudFlags()).contains("SANCTIONED_JURISDICTION");
    }

    @Test
    @DisplayName("Normal small transaction passes all rules")
    void normalTransaction_shouldPass() {
        TransactionFact tx = TransactionFact.builder()
            .transactionId(UUID.randomUUID())
            .amount(new BigDecimal("250.00"))
            .fromCountry("MT")
            .toCountry("DE")
            .transactionsLastHour(1)
            .isOffHours(false)
            .build();

        session.insert(tx);
        session.fireAllRules();

        assertThat(tx.isBlocked()).isFalse();
        assertThat(tx.isFiauReport()).isFalse();
    }
}
