package com.aurum.core_banking.common.exception;

public class TransactionBlockedException extends RuntimeException {

    public TransactionBlockedException(String reason) {
        super("Transaction blocked by fraud/AML rules: " + reason);
    }
}
