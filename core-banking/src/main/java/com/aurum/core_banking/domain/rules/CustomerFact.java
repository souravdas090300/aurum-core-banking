package com.aurum.core_banking.domain.rules;

import lombok.*;

/**
 * Customer fact for AML / sanctions screening.
 */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerFact {

    private String  customerId;
    private String  fullName;
    private String  nationalId;
    private String  countryCode;
    private boolean isPep;            // Politically Exposed Person
    private int     creditScore;      // 300 – 850
    private double  debtToIncomeRatio;
    private double  monthlyIncome;
    private double  requestedAmount;
    private int     termMonths;

    // ── Rule output fields ──
    private String  sanctionsMatch;   // name of matched entity if found
    @Builder.Default
    private boolean amlAlert   = false;
    private String  loanDecision;     // APPROVED / REVIEW / DECLINED
    private int     riskScore;        // 0-100
}