package com.aurum.core_banking.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics exposed via /actuator/prometheus.
 * Scraped by Prometheus every 15s and visualised in Grafana.
 *
 * Metric naming convention: banking.<domain>.<event>
 */
@Slf4j
@Component
public class BankingMetrics {

    // ── Counters ──────────────────────────────────────────────────────────────
    private final Counter transferSuccessCounter;
    private final Counter transferFailedCounter;
    private final Counter transferBlockedCounter;
    private final Counter fiauReportsCounter;
    private final Counter loanApplicationsCounter;
    private final Counter sanctionsHitsCounter;

    // ── Timers ────────────────────────────────────────────────────────────────
    private final Timer transferProcessingTimer;
    private final Timer fraudEvaluationTimer;
    private final Timer creditScoringTimer;

    // ── Gauges (instantaneous state) ─────────────────────────────────────────
    private final AtomicInteger activeTransactions = new AtomicInteger(0);
    private final AtomicInteger pendingLoanReviews  = new AtomicInteger(0);

    // ── Distribution summaries ────────────────────────────────────────────────
    private final DistributionSummary transferAmountSummary;

    public BankingMetrics(MeterRegistry registry) {

        transferSuccessCounter = Counter.builder("banking.transfers.success")
                .description("Total successful fund transfers")
                .tag("app", "aurum-core-banking")
                .register(registry);

        transferFailedCounter = Counter.builder("banking.transfers.failed")
                .description("Total failed fund transfers")
                .register(registry);

        transferBlockedCounter = Counter.builder("banking.transfers.blocked")
                .description("Transfers blocked by fraud detection")
                .register(registry);

        fiauReportsCounter = Counter.builder("banking.compliance.fiau_reports")
                .description("Transactions flagged for FIAU reporting")
                .register(registry);

        loanApplicationsCounter = Counter.builder("banking.loans.applications")
                .description("Total loan applications submitted")
                .register(registry);

        sanctionsHitsCounter = Counter.builder("banking.compliance.sanctions_hits")
                .description("Sanctions list matches detected")
                .register(registry);

        transferProcessingTimer = Timer.builder("banking.transfers.processing.time")
                .description("Time to process a fund transfer end-to-end")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        fraudEvaluationTimer = Timer.builder("banking.fraud.evaluation.time")
                .description("Time for Drools fraud rule evaluation")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        creditScoringTimer = Timer.builder("banking.credit.scoring.time")
                .description("Time for Drools credit scoring evaluation")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        Gauge.builder("banking.transactions.active",
                        activeTransactions, AtomicInteger::get)
                .description("Currently processing transactions")
                .register(registry);

        Gauge.builder("banking.loans.pending_review",
                        pendingLoanReviews, AtomicInteger::get)
                .description("Loan applications awaiting officer review")
                .register(registry);

        transferAmountSummary = DistributionSummary.builder("banking.transfers.amount")
                .description("Distribution of transfer amounts in EUR")
                .baseUnit("EUR")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(registry);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void recordTransferSuccess(BigDecimal amount) {
        transferSuccessCounter.increment();
        transferAmountSummary.record(amount.doubleValue());
    }

    public void recordTransferFailed()  { transferFailedCounter.increment(); }
    public void recordTransferBlocked() { transferBlockedCounter.increment(); }
    public void recordFiauReport()      { fiauReportsCounter.increment(); }
    public void recordLoanApplication() { loanApplicationsCounter.increment(); }
    public void recordSanctionsHit()    { sanctionsHitsCounter.increment(); }

    public Timer.Sample startTransferTimer() { return Timer.start(); }
    public Timer.Sample startFraudTimer()    { return Timer.start(); }
    public Timer.Sample startCreditTimer()   { return Timer.start(); }

    public void stopTransferTimer(Timer.Sample sample) { sample.stop(transferProcessingTimer); }
    public void stopFraudTimer(Timer.Sample sample)    { sample.stop(fraudEvaluationTimer); }
    public void stopCreditTimer(Timer.Sample sample)   { sample.stop(creditScoringTimer); }

    public void incrementActiveTransactions() { activeTransactions.incrementAndGet(); }
    public void decrementActiveTransactions() { activeTransactions.decrementAndGet(); }
    public void setPendingLoanReviews(int n)  { pendingLoanReviews.set(n); }
}
