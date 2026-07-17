package com.aurum.corebanking.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotNull private UUID fromAccountId;
    @NotNull private UUID toAccountId;
    @NotNull @Positive private BigDecimal amount;
    @NotNull private String currency;
    private String reference;
    @NotNull private String idempotencyKey;

    // Explicit getters for Lombok compatibility
    public UUID fromAccountId() {
        return fromAccountId;
    }

    public UUID toAccountId() {
        return toAccountId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public String reference() {
        return reference;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }
}