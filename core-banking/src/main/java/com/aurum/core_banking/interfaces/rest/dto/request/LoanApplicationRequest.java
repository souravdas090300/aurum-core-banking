package com.aurum.core_banking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record LoanApplicationRequest(
    @NotNull UUID customerId,
    @NotBlank String fullName,
    @NotNull @Positive @DecimalMax("9999999.9999") BigDecimal amount,
    @NotNull @Min(1) @Max(360) Integer termMonths,
    @Min(300) @Max(850) int creditScore,
    @DecimalMin("0.0") @DecimalMax("1.0") double debtToIncomeRatio,
    @Positive double monthlyIncome,
    boolean pep
) {
    public UUID getCustomerId()          { return customerId; }
    public String getFullName()          { return fullName; }
    public BigDecimal getAmount()        { return amount; }
    public Integer getTermMonths()       { return termMonths; }
    public int getCreditScore()          { return creditScore; }
    public double getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public double getMonthlyIncome()     { return monthlyIncome; }
    public boolean isPep()               { return pep; }
}
