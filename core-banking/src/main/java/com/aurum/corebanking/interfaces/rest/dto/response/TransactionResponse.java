package com.aurum.corebanking.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction response returned after a successful transaction operation.
 */
public record TransactionResponse(
    UUID       transactionId,
    String     type,           // DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT
    UUID       accountId,
    BigDecimal amount,
    String     currency,
    String     reference,
    String     status,
    Instant    executedAt
) {}
