package com.aurum.core_banking.application.service;

import com.aurum.core_banking.common.audit.AuditService;
import com.aurum.core_banking.infrastructure.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Malta FIAU (Financial Intelligence Analysis Unit) Reporting.
 *
 * Legal basis: Prevention of Money Laundering Act (PMLA) — Malta
 * Threshold:   All transactions ≥ €10,000 must be reported.
 * Deadline:    Report within 5 working days of detection.
 *
 * In production: submit XML reports via FIAU's GOAML portal API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiauReportService {

    private final TransactionRepository transactionRepository;
    private final AuditService          auditService;

    /**
     * Runs daily at 06:00 UTC.
     * Collects all transactions flagged for FIAU reporting in the past 24 hours
     * and generates the mandatory report.
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "UTC")
    public void generateDailyFiauReport() {
        Instant from = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant to   = Instant.now();

        List<?> flaggedTransactions = transactionRepository
                .findByFraudFlagAndCreatedAtBetween("FIAU_REPORT_REQUIRED", from, to);

        if (flaggedTransactions.isEmpty()) {
            log.info("FIAU daily report: no reportable transactions in period {} to {}", from, to);
            return;
        }

        log.info("FIAU report generated — {} transactions for period {} to {}",
                flaggedTransactions.size(), from, to);

        // Production: submit to FIAU GOAML portal via XML API
        // submitToGoAml(flaggedTransactions);

        auditService.log(
                "FIAU_REPORT", null,
                "DAILY_REPORT_GENERATED", "SYSTEM",
                null,
                "Transactions reported: " + flaggedTransactions.size()
                        + " | period: " + from + " to " + to);
    }
}
