package com.aurum.corebanking.infrastructure.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BankingMetrics {

    private final Counter transferSuccessCounter;
    private final Counter transferFailedCounter;
    private final Counter transferBlockedCounter;
    private final Counter fiauReportsCounter;
    private final Counter loanApplicationsCounter;
    private final Counter sanctionsHitsCounter;
    private final Timer   transferProcessingTimer;
    private final Timer   fraudEvaluationTimer;
    private final Timer   creditScoringTimer;
    private final DistributionSummary transferAmountSummary;

    private final AtomicInteger activeTransactions = new AtomicInteger(0);
    private final AtomicInteger pendingLoanReviews = new AtomicInteger(0);

    public BankingMetrics(MeterRegistry registry) {

        transferSuccessCounter = Counter.builder("banking.transfers.success")
            .description("Total successful fund transfers")
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
            .description("Transfer processing time end-to-end")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);

        fraudEvaluationTimer = Timer.builder("banking.fraud.evaluation.time")
            .description("Drools fraud rule evaluation time")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);

        creditScoringTimer = Timer.builder("banking.credit.scoring.time")
            .description("Drools credit scoring evaluation time")
            .register(registry);

        transferAmountSummary = DistributionSummary
            .builder("banking.transfers.amount")
            .description("Distribution of transfer amounts in EUR")
            .baseUnit("EUR")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .register(registry);

        Gauge.builder("banking.transactions.active",
                      activeTransactions, AtomicInteger::get)
            .description("Currently processing transactions")
            .register(registry);

        Gauge.builder("banking.loans.pending_review",
                      pendingLoanReviews, AtomicInteger::get)
            .description("Loan applications awaiting officer review")
            .register(registry);
    }

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

    public void stopTransferTimer(Timer.Sample s) { s.stop(transferProcessingTimer); }
    public void stopFraudTimer(Timer.Sample s)    { s.stop(fraudEvaluationTimer); }
    public void stopCreditTimer(Timer.Sample s)   { s.stop(creditScoringTimer); }

    public void incrementActiveTransactions() { activeTransactions.incrementAndGet(); }
    public void decrementActiveTransactions() { activeTransactions.decrementAndGet(); }
    public void setPendingLoanReviews(int n)  { pendingLoanReviews.set(n); }
}