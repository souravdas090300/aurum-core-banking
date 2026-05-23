package com.aurum.corebanking.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transfer response returned after a successful (or idempotent) transfer.
 */
public record TransferResponse(
    UUID       transactionId,
    String     status,
    BigDecimal amount,
    String     currency,
    String     reference,
    Instant    executedAt
) {}
