package com.aurum.core_banking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

    @NotNull(message = "Source account is required")
    UUID fromAccountId,

    @NotNull(message = "Destination account is required")
    UUID toAccountId,

    @NotNull
    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "999999.9999", message = "Amount exceeds single transfer limit")
    BigDecimal amount,

    @NotBlank
    @Size(max = 3)
    String currency,

    @Size(max = 140)
    String reference,

    // Client generates this UUID — server uses it to deduplicate (PSD2 requirement)
    @NotBlank
    @Size(max = 64)
    String idempotencyKey
) {}
