package com.aurum.corebanking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deposit request for adding funds to an account.
 *
 * <p>The {@code idempotencyKey} (max 64 chars) must be unique per deposit — clients
 * should retry with the same key to safely replay a failed request.
 */
public record DepositRequest(

    @NotNull(message = "accountId is required")
    UUID accountId,

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @Digits(integer = 15, fraction = 4, message = "amount exceeds precision limits")
    BigDecimal amount,

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO 4217 code")
    String currency,

    @Size(max = 140, message = "reference must not exceed 140 characters")
    String reference,

    @NotBlank(message = "idempotencyKey is required")
    @Size(max = 64, message = "idempotencyKey must not exceed 64 characters")
    String idempotencyKey
) {}
