package com.aurum.core_banking.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
    UUID transactionId,
    String status,
    BigDecimal amount,
    String currency,
    String reference,
    Instant executedAt
) {}
