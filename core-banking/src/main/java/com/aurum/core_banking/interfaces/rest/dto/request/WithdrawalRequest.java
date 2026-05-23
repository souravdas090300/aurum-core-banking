package com.aurum.core_banking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    
    @NotNull
    private UUID accountId;
    
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    
    @Size(max = 140)
    private String reference;
}
