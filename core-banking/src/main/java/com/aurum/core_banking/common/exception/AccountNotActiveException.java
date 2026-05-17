package com.aurum.core_banking.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(UUID accountId, String status) {
        super("Account " + accountId + " is not active (status=" + status + ")");
    }
}
