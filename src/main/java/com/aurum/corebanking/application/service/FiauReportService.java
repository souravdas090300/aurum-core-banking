package com.aurum.corebanking.application.service;

import com.aurum.corebanking.common.audit.AuditService;
import com.aurum.corebanking.infrastructure.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Malta FIAU (Financial Intelligence Analysis Unit) Reporting.
 * Legal basis: Prevention of Money Laundering Act (PMLA) — Malta.
 * Threshold: All transactions >= EUR 10,000 must be reported.
 * Deadline: Within 5 working days of detection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiauReportService {

    private final TransactionRepository transactionRepository;
    private final AuditService          auditService;

    @Scheduled(cron = "0 0 6 * * *", zone = "UTC")
    public void generateDailyFiauReport() {
        Instant from = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant to   = Instant.now();

        List<?> flagged = transactionRepository
            .findByFraudFlagAndCreatedAtBetween("FIAU_REPORT_REQUIRED", from, to);

        if (flagged.isEmpty()) {
            log.info("FIAU daily report: no reportable transactions");
            return;
        }

        log.info("FIAU report generated — {} transactions for period {} to {}",
            flagged.size(), from, to);

        // In production: submit to FIAU GOAML XML portal
        // goamlService.submit(flagged);

        // FIXED: Use recordSystem instead of log()
        auditService.recordSystem("FIAU_REPORT", null, "DAILY_REPORT_GENERATED",
            "Transactions reported: " + flagged.size());
    }
}