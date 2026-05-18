package com.aurum.core_banking.domain.rules;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Drools working-memory fact for customer KYC, AML and credit-scoring rules.
 *
 * Fields prefixed with "output" are written by rules; callers should read them
 * after {@code fireAllRules()} returns.
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
    private boolean isPep;
    private int     creditScore;
    private double  debtToIncomeRatio;
    private double  monthlyIncome;
    private double  requestedAmount;
    private int     termMonths;

    // ── Output fields (written by Drools rules) ───────────────────────────
    private String sanctionsMatch;

    @Builder.Default
    private boolean amlAlert = false;

    /** APPROVED | REVIEW | DECLINED */
    private String loanDecision;

    /** 0–100, higher = riskier */
    private int riskScore;
}
