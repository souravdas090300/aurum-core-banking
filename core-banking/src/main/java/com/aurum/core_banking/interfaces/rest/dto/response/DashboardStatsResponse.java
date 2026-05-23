package com.aurum.core_banking.interfaces.rest.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private BigDecimal totalBalance;
    private Integer totalAccounts;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlySpending;
}
