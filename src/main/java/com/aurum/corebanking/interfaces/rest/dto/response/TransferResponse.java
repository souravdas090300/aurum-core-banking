package com.aurum.corebanking.interfaces.rest.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransferResponse {
    private UUID transactionId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private Instant executedAt;
}