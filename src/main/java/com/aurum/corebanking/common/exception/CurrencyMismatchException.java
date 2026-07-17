package com.aurum.corebanking.common.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String message) {
        super(message);
    }
    public CurrencyMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}