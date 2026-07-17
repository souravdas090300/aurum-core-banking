package com.aurum.corebanking.domain.rules;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Drools working-memory fact for fraud-detection and AML rules.
 *
 * Fields prefixed with "output" are written by rules; callers should read them
 * after {@code fireAllRules()} returns.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFact {

    // ── Input fields ──────────────────────────────────────────────────────
    private String        transactionId;
    private String        fromAccountId;
    private String        toAccountId;
    private BigDecimal    amount;
    private String        currency;
    private String        fromCountry;
    private String        toCountry;
    private String        customerName;
    private LocalDateTime timestamp;

    /** Number of transactions from this account in the last hour. */
    private int transactionsLastHour;

    /** True if the transaction timestamp is between 22:00 and 06:00. */
    private boolean offHours;

    // ── Output fields (written by Drools rules) ───────────────────────────
    @Builder.Default
    private List<String> fraudFlags = new ArrayList<>();

    @Builder.Default
    private boolean blocked = false;

    private String blockReason;

    /** True if a FIAU report must be filed for this transaction. */
    @Builder.Default
    private boolean fiauReport = false;
}
