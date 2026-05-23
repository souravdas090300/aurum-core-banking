package com.aurum.core_banking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentRequest {
    
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    
    @Size(max = 140)
    private String reference;
}
