package com.aurum.core_banking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Loan application request submitted by a customer.
 */
public record LoanApplicationRequest(

    @NotNull
    UUID customerId,

    @NotBlank
    String fullName,

    @NotNull
    @DecimalMin("1000.00")
    BigDecimal amount,

    @NotNull
    @Min(6)
    @Max(360)
    Integer termMonths,

    @Min(300)
    @Max(850)
    int creditScore,

    @DecimalMin("0.00")
    @DecimalMax("2.00")
    double debtToIncomeRatio,

    @DecimalMin("0.00")
    double monthlyIncome,

    boolean isPep
) {}
