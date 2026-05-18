package com.aurum.core_banking.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String accountCurrency, String requestCurrency) {
        super("Currency mismatch: account currency is " + accountCurrency
                + " but transfer requested " + requestCurrency);
    }
}
