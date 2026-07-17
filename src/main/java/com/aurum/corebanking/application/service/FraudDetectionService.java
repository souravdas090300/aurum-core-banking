package com.aurum.corebanking.application.service;

import com.aurum.corebanking.domain.rules.TransactionFact;
import com.aurum.corebanking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.corebanking.infrastructure.rules.SanctionsListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Fraud and AML detection service.
 *
 * <p>Builds a {@link TransactionFact} from the transfer parameters, runs it through
 * Drools fraud-detection and AML-screening rules, then returns a {@link FraudResult}
 * summarising whether the transaction should be blocked.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final KieContainer           kieContainer;
    private final SanctionsListService   sanctionsListService;
    private final TransactionRepository  transactionRepository;

    /**
     * Evaluate a pending transfer for fraud and AML risks.
     *
     * @param transactionId  provisional transaction UUID (for logging)
     * @param fromAccountId  debit account
     * @param toAccountId    credit account
     * @param amount         transfer amount
     * @param currency       ISO 4217 currency code
     * @param fromCountry    sender's country (ISO 3166-1 alpha-2)
     * @param toCountry      beneficiary's country
     * @param customerName   sender's full name (for sanctions screening)
     * @return {@link FraudResult} with all detected flags
     */
    public FraudResult evaluate(UUID transactionId, UUID fromAccountId, UUID toAccountId,
                                BigDecimal amount, String currency,
                                String fromCountry, String toCountry, String customerName) {

        // 1. Velocity check — count recent transactions from this account
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        int recentTxCount = transactionRepository.countByFromAccountIdAndCreatedAtAfter(
                fromAccountId, oneHourAgo);

        // 2. Off-hours check
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int hour = now.getHour();
        boolean offHours = (hour >= 22 || hour < 6);

        // 3. Sanctions screening (before Drools so we can set the flag on the fact)
        boolean sanctioned = sanctionsListService.screen(customerName, null) != null;

        // 4. Build the working-memory fact
        TransactionFact fact = TransactionFact.builder()
                .transactionId(transactionId.toString())
                .fromAccountId(fromAccountId.toString())
                .toAccountId(toAccountId.toString())
                .amount(amount)
                .currency(currency)
                .fromCountry(fromCountry)
                .toCountry(toCountry)
                .customerName(customerName)
                .timestamp(now)
                .transactionsLastHour(recentTxCount)
                .offHours(offHours)
                .build();

        // 5. Run Drools rules
        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(fact);
            int fired = session.fireAllRules();
            log.debug("Fraud rules: {} fired for txId={}", fired, transactionId);
        } finally {
            session.dispose();
        }

        // 6. Overlay sanctions result
        if (sanctioned) {
            fact.setBlocked(true);
            fact.setBlockReason("SANCTIONS_MATCH");
            fact.getFraudFlags().add("SANCTIONS_MATCH");
        }

        return FraudResult.builder()
                .blocked(fact.isBlocked())
                .blockReason(fact.getBlockReason())
                .fraudFlags(fact.getFraudFlags())
                .fiauReportRequired(fact.isFiauReport())
                .amlAlert(!fact.getFraudFlags().isEmpty())
                .build();
    }
}
