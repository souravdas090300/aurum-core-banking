package com.aurum.corebanking.rules;

import com.aurum.corebanking.domain.rules.TransactionFact;
import com.aurum.corebanking.infrastructure.rules.DroolsConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Drools fraud-detection rules.
 *
 * <p>Loads only the {@link DroolsConfig} bean — no full Spring Boot context needed.
 * Each test creates a fresh KieSession to avoid cross-test contamination.
 */
@SpringBootTest(classes = DroolsConfig.class)
@DisplayName("Fraud Detection Rules")
class FraudDetectionRulesTest {

    @Autowired
    KieContainer kieContainer;

    private KieSession session;

    @BeforeEach
    void setUp() {
        // Try to create session with explicit name, fall back to default if not found
        try {
            session = kieContainer.newKieSession("ksession-rules");
            System.out.println("[TEST] Created KieSession: ksession-rules");
        } catch (Exception e) {
            System.out.println("[TEST] Failed to create named session, trying fallback: " + e.getMessage());
            // If named session not found, try getting the first available session
            var kieBaseNames = kieContainer.getKieBaseNames();
            System.out.println("[TEST] Available KieBases: " + kieBaseNames);
            if (kieBaseNames != null && !kieBaseNames.isEmpty()) {
                String kieBaseName = kieBaseNames.iterator().next();
                var kieSessionNames = kieContainer.getKieSessionNamesInKieBase(kieBaseName);
                System.out.println("[TEST] Available KieSessions in " + kieBaseName + ": " + kieSessionNames);
                if (kieSessionNames != null && !kieSessionNames.isEmpty()) {
                    String sessionName = kieSessionNames.iterator().next();
                    session = kieContainer.newKieSession(sessionName);
                    System.out.println("[TEST] Created KieSession: " + sessionName);
                }
            }
        }
        
        if (session == null) {
            throw new IllegalStateException(
                "Could not create KieSession. Available KieBases: " + kieContainer.getKieBaseNames()
            );
        }
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            session.dispose();
        }
    }

    @Test
    @DisplayName("Transaction ≥ €10,000 triggers FIAU mandatory report flag")
    void largeTransaction_shouldSetFiauFlag() {
        TransactionFact fact = TransactionFact.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccountId("acc-001")
                .toAccountId("acc-002")
                .amount(new BigDecimal("12000.00"))
                .currency("EUR")
                .fromCountry("MT")
                .toCountry("MT")
                .customerName("Alice Smith")
                .timestamp(LocalDateTime.now())
                .transactionsLastHour(1)
                .offHours(false)
                .build();

        System.out.println("[TEST] Before insert - fiauReport: " + fact.isFiauReport() + ", blocked: " + fact.isBlocked() + ", flags: " + fact.getFraudFlags());
        session.insert(fact);
        int rulesFired = session.fireAllRules();
        System.out.println("[TEST] Rules fired: " + rulesFired);
        System.out.println("[TEST] After fireAllRules - fiauReport: " + fact.isFiauReport() + ", blocked: " + fact.isBlocked() + ", flags: " + fact.getFraudFlags());
        System.out.println("[TEST] Fact amount: " + fact.getAmount() + ", transactionsLastHour: " + fact.getTransactionsLastHour());

        assertThat(fact.isFiauReport()).isTrue();
        assertThat(fact.isBlocked()).isFalse();
        assertThat(fact.getFraudFlags()).contains("FIAU_REPORT_REQUIRED");
    }

    @Test
    @DisplayName("More than 5 transactions/hour triggers velocity breach block")
    void velocityBreach_shouldBlockTransaction() {
        TransactionFact fact = TransactionFact.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccountId("acc-003")
                .toAccountId("acc-004")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .fromCountry("MT")
                .toCountry("MT")
                .customerName("Bob Jones")
                .timestamp(LocalDateTime.now())
                .transactionsLastHour(7)   // > 5 threshold
                .offHours(false)
                .build();

        session.insert(fact);
        session.fireAllRules();

        assertThat(fact.isBlocked()).isTrue();
        assertThat(fact.getFraudFlags()).anyMatch(f -> f.contains("VELOCITY"));
    }

    @Test
    @DisplayName("Transfer to sanctioned country (IR) is hard-blocked")
    void sanctionedCountry_shouldBeBlocked() {
        TransactionFact fact = TransactionFact.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccountId("acc-005")
                .toAccountId("acc-006")
                .amount(new BigDecimal("500.00"))
                .currency("EUR")
                .fromCountry("MT")
                .toCountry("IR")            // Iran — on OFAC/FATF high-risk list
                .customerName("Carol White")
                .timestamp(LocalDateTime.now())
                .transactionsLastHour(1)
                .offHours(false)
                .build();

        session.insert(fact);
        session.fireAllRules();

        assertThat(fact.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("Normal transaction under €10k with no risk factors passes without flags")
    void normalTransaction_shouldPass() {
        TransactionFact fact = TransactionFact.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccountId("acc-007")
                .toAccountId("acc-008")
                .amount(new BigDecimal("250.00"))
                .currency("EUR")
                .fromCountry("MT")
                .toCountry("DE")
                .customerName("Dave Brown")
                .timestamp(LocalDateTime.now())
                .transactionsLastHour(2)
                .offHours(false)
                .build();

        session.insert(fact);
        session.fireAllRules();

        assertThat(fact.isBlocked()).isFalse();
        assertThat(fact.isFiauReport()).isFalse();
    }
}
