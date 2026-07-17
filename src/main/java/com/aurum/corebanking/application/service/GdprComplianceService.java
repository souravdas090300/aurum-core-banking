package com.aurum.corebanking.application.service;

import com.aurum.corebanking.common.audit.AuditService;
import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.corebanking.infrastructure.persistence.entity.CustomerEntity;
import com.aurum.corebanking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.corebanking.infrastructure.persistence.repository.CustomerRepository;
import com.aurum.corebanking.infrastructure.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GdprComplianceService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    /**
     * Check if customer has active accounts
     */
    private boolean hasActiveAccounts(UUID customerId) {
        List<AccountEntity> accounts = accountRepository.findByCustomerId(customerId);
        return accounts.stream().anyMatch(a -> a.getStatus() == AccountEntity.AccountStatus.ACTIVE);
    }

    @Transactional
    public void processErasureRequest(UUID customerId, String requestedBy) {
        var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        // Check if customer has active accounts
        if (hasActiveAccounts(customerId)) {
            throw new IllegalStateException("Customer has active accounts - cannot erase data");
        }

        // Anonymize customer data
        customer.setFirstName("ANONYMIZED");
        customer.setLastName("ANONYMIZED");
        customer.setEmail("deleted_" + customerId + "@example.com");
        
        customerRepository.save(customer);
        auditService.record("Customer", customerId, "GDPR_ERASURE", requestedBy, "Customer data anonymized");
        log.info("GDPR erasure completed for customer: {}", customerId);
    }

    /**
     * Scheduled weekly job to auto-anonymise customers eligible for retention deletion.
     * Banking retention period: 7 years after account closure.
     * Runs every Sunday at 2 AM UTC.
     */
    @Scheduled(cron = "0 0 2 ? * SUN", zone = "UTC")
    @Transactional
    public void scheduledRetentionCleanup() {
        Instant cutoffDate = Instant.now().minus(7, ChronoUnit.YEARS);
        
        // Find customers with no active accounts and created before cutoff
        List<CustomerEntity> eligibleCustomers = customerRepository.findAll().stream()
            .filter(c -> !hasActiveAccounts(c.getId()))
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isBefore(cutoffDate))
            .toList();

        if (eligibleCustomers.isEmpty()) {
            log.info("GDPR retention cleanup: no eligible customers found");
            return;
        }

        int anonymizedCount = 0;
        for (CustomerEntity customer : eligibleCustomers) {
            try {
                customer.setFirstName("ANONYMIZED");
                customer.setLastName("ANONYMIZED");
                customer.setEmail("retention_" + customer.getId() + "@example.com");
                customer.setNationalId("ANONYMIZED");
                customerRepository.save(customer);
                anonymizedCount++;
            } catch (Exception e) {
                log.error("Failed to anonymize customer {}: {}", customer.getId(), e.getMessage());
            }
        }

        log.info("GDPR retention cleanup completed: {} customers anonymized", anonymizedCount);
        auditService.recordSystem("GDPR_RETENTION", null, "WEEKLY_CLEANUP",
            "Auto-anonymized " + anonymizedCount + " customers eligible for retention deletion");
    }
}