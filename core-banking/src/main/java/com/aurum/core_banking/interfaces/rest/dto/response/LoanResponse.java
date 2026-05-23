package com.aurum.core_banking.interfaces.rest.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private UUID id;
    private UUID customerId;
    private UUID accountId;
    private String loanType;
    private BigDecimal principalAmount;
    private BigDecimal remainingBalance;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private LocalDate nextPaymentDate;
    private String status;
    private Instant approvedAt;
    private Instant disbursedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
