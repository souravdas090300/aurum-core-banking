package com.aurum.core_banking.common.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String requested, String actual) {
        super("Currency mismatch: requested " + requested + " but account currency is " + actual);
    }
}
