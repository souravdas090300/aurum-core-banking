package com.aurum.core_banking.interfaces.rest.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private String idempotencyKey;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String status;
    private String reference;
    private String fraudFlag;
    private Instant executedAt;
    private Instant createdAt;
}
