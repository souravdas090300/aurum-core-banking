package com.aurum.core_banking.common.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, BigDecimal balance, BigDecimal requested) {
        super("Insufficient funds on account " + accountId
                + ": balance=" + balance + ", requested=" + requested);
    }
}
