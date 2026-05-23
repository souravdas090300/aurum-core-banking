package com.aurum.core_banking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {
    
    @NotNull
    private UUID customerId;
    
    @NotBlank
    private String accountType;
    
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
    
    @DecimalMin("0.0")
    private BigDecimal initialDeposit;
}
