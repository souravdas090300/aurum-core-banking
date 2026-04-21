package com.aurum.core_banking.application.service;

import com.aurum.core_banking.domain.rules.CustomerFact;
import com.aurum.core_banking.domain.rules.TransactionFact;
import com.aurum.core_banking.infrastructure.rules.SanctionsListService;
import com.aurum.core_banking.infrastructure.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final KieContainer       kieContainer;
    private final SanctionsListService sanctionsList;
    private final TransactionRepository transactionRepository;

    public FraudResult evaluate(TransactionFact txFact, CustomerFact customerFact) {

        // ── 1. Enrich facts before rules fire ──
        enrichTransactionFact(txFact);
        enrichCustomerFact(customerFact);

        // ── 2. Create STATELESS session per evaluation (thread-safe) ──
        KieSession session = kieContainer.newKieSession();

        try {
            // Insert facts into Drools working memory
            session.insert(txFact);
            session.insert(customerFact);

            // Fire ALL matching rules
            int rulesFired = session.fireAllRules();
            log.debug("Fraud evaluation: {} rules fired for tx={}",
                rulesFired, txFact.getTransactionId());

        } finally {
            session.dispose(); // always release session resources
        }

        // ── 3. Collect results ──
        FraudResult result = FraudResult.builder()
            .blocked(txFact.isBlocked())
            .blockReason(txFact.getBlockReason())
            .fraudFlags(txFact.getFraudFlags())
            .fiauReportRequired(txFact.isFiauReport())
            .amlAlert(customerFact.isAmlAlert())
            .build();

        if (result.isBlocked()) {
            log.warn("TRANSACTION BLOCKED — id={} reason={} flags={}",
                txFact.getTransactionId(),
                result.getBlockReason(),
                result.getFraudFlags());
        }

        return result;
    }

    private void enrichTransactionFact(TransactionFact tx) {
        // Check off-hours (22:00 – 06:00 Malta/EU time)
        LocalTime now = LocalTime.now();
        tx.setOffHours(now.isAfter(LocalTime.of(22, 0))
                    || now.isBefore(LocalTime.of(6, 0)));

        // Count recent transactions for velocity check
        long recentCount = transactionRepository
            .countByFromAccountIdAndCreatedAtAfter(
                tx.getFromAccountId(),
                java.time.Instant.now().minusSeconds(3600));
        tx.setTransactionsLastHour((int) recentCount);
    }

    private void enrichCustomerFact(CustomerFact customer) {
        // Check OFAC + EU sanctions list
        String match = sanctionsList.screen(customer.getFullName(),
                                            customer.getNationalId());
        customer.setSanctionsMatch(match);
    }
}