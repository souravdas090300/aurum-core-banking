package com.aurum.corebanking.common.exception;

public class TransactionBlockedException extends RuntimeException {
    public TransactionBlockedException(String message) {
        super(message);
    }
    public TransactionBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}