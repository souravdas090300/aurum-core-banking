package com.aurum.core_banking.application.service;

import com.aurum.core_banking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.core_banking.infrastructure.persistence.entity.AuditLogEntity;
import com.aurum.core_banking.infrastructure.persistence.entity.TransactionEntity;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * GDPR Article 15 — Right of Access data export.
 * Contains all personal data held about a customer in portable format.
 */
@Builder
public record GdprDataExport(
    UUID                   customerId,
    List<AccountEntity>    accounts,
    List<TransactionEntity> transactions,
    List<AuditLogEntity>   auditHistory,
    Instant                exportedAt
) {}
