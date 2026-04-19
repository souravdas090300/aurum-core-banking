package com.aurum.core_banking.common.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(UUID accountId, BigDecimal amount) {
        super("Insufficient funds in account " + accountId + " for amount " + amount);
    }
}
