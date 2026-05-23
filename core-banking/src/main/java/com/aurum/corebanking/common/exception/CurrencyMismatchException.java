package com.aurum.corebanking.common.exception;

public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String accountCurrency, String requestCurrency) {
        super("Currency mismatch: account currency is " + accountCurrency
                + " but transfer requested " + requestCurrency);
    }
}
