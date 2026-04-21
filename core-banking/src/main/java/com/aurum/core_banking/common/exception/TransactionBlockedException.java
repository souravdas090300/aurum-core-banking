package com.aurum.core_banking.common.exception;

import java.util.List;

public class TransactionBlockedException extends RuntimeException {

    private final List<String> fraudFlags;

    public TransactionBlockedException(String blockReason, List<String> fraudFlags) {
        super("Transaction blocked: " + blockReason);
        this.fraudFlags = fraudFlags;
    }

    public List<String> getFraudFlags() {
        return fraudFlags;
    }
}
