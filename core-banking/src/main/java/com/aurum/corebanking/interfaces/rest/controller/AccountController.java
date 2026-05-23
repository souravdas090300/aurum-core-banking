package com.aurum.corebanking.interfaces.rest.controller;

import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.corebanking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.corebanking.common.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for account enquiry operations.
 *
 * <p>Write operations (opening, closing, freezing accounts) are out of scope for
 * this prototype and would be added in subsequent iterations.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    /**
     * Retrieve account details by internal UUID.
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<AccountEntity> getAccount(@PathVariable UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return ResponseEntity.ok(account);
    }

    /**
     * Retrieve account details by IBAN account number.
     */
    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<AccountEntity> getAccountByNumber(@PathVariable String accountNumber) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return ResponseEntity.ok(account);
    }
}
