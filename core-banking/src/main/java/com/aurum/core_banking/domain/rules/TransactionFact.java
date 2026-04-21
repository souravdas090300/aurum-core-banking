package com.aurum.core_banking.domain.rules;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fact object inserted into Drools working memory.
 * Plain POJO — zero framework dependencies in domain layer.
 */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionFact {

    private UUID        transactionId;
    private UUID        fromAccountId;
    private UUID        toAccountId;
    private BigDecimal  amount;
    private String      currency;
    private String      fromCountry;
    private String      toCountry;
    private String      customerName;
    private Instant     timestamp;
    private int         transactionsLastHour;   // populated before rules fire
    private boolean     isOffHours;             // 22:00 - 06:00

    // ── Rule output fields — Drools writes into these ──
    @Builder.Default
    private List<String>    fraudFlags   = new ArrayList<>();
    @Builder.Default
    private boolean         blocked      = false;
    private String          blockReason;
    @Builder.Default
    private boolean         fiauReport   = false;  // Malta FIAU threshold flag
}