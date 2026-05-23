package com.aurum.corebanking.common.exception;

import java.util.UUID;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(UUID accountId, String status) {
        super("Account " + accountId + " is not active (status=" + status + ")");
    }
}
