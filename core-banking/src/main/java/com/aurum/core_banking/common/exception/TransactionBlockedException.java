package com.aurum.core_banking.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class TransactionBlockedException extends RuntimeException {

    public TransactionBlockedException(String reason) {
        super("Transaction blocked by fraud/AML rules: " + reason);
    }
}
