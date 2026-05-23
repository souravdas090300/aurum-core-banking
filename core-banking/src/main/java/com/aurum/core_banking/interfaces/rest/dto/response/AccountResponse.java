package com.aurum.core_banking.interfaces.rest.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private UUID id;
    private String accountNumber;
    private UUID customerId;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private String currency;
    private BigDecimal creditLimit;
    private Instant openedAt;
    private Instant closedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
