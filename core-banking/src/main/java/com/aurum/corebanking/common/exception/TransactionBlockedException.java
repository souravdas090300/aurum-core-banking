package com.aurum.corebanking.common.exception;

public class TransactionBlockedException extends RuntimeException {

    public TransactionBlockedException(String reason) {
        super("Transaction blocked by fraud/AML rules: " + reason);
    }
}
