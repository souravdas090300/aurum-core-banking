package com.aurum.corebanking.domain.rules;

import lombok.*;

/**
 * Drools working-memory fact for customer KYC, AML and credit-scoring rules.
 *
 * IMPORTANT — field naming rules for Drools compatibility:
 *
 * 1. Boolean fields must NOT use the "is" prefix. Lombok generates isX() for
 *    boolean fields named "x", which works fine in Java but Drools uses
 *    JavaBeans introspection and may generate getX() accessor names in some
 *    contexts. To avoid all ambiguity, boolean fields use plain names: pep,
 *    amlAlert — never isPep, isAmlAlert.
 *
 * 2. The sanctionsMatch field is a String (the matched entity name) not a
 *    boolean. This gives Drools a clean getSanctionsMatch() getter and lets
 *    rules distinguish "no match" (null) from "matched X" (name string).
 *    The aml-screening.drl checks: sanctionsMatch != null
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFact {

    // ── Input fields (set by caller) ──────────────────────────────────────
    private String  customerId;
    private String  fullName;
    private String  nationalId;
    private String  countryCode;

    /** Politically Exposed Person flag — triggers Enhanced Due Diligence. */
    private boolean pep;

    private int     creditScore;        // 300–850
    private double  debtToIncomeRatio;
    private double  monthlyIncome;
    private double  requestedAmount;
    private int     termMonths;

    // ── Output fields (written by Drools rules) ───────────────────────────

    /**
     * Name of the matched sanctions entry, or null if no match.
     * String type ensures Drools generates getSanctionsMatch() — a plain
     * boolean would generate isSanctionsMatch() which DRL cannot call.
     */
    private String  sanctionsMatch;

    @Builder.Default
    private boolean amlAlert = false;

    /** APPROVED | REVIEW | DECLINED — set by credit-scoring.drl */
    private String  loanDecision;

    /** 0–100, higher = riskier — set by rules */
    private int     riskScore;
}
