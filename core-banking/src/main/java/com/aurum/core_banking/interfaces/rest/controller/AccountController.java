package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.core_banking.interfaces.rest.dto.request.AccountCreateRequest;
import com.aurum.core_banking.interfaces.rest.dto.response.AccountResponse;
import com.aurum.core_banking.common.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for account management operations.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AccountController {

    private final AccountRepository accountRepository;

    /**
     * Get all accounts.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = accountRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    /**
     * Retrieve account details by internal UUID.
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable @NonNull UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return ResponseEntity.ok(toResponse(account));
    }

    /**
     * Retrieve account details by IBAN account number.
     */
    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return ResponseEntity.ok(toResponse(account));
    }

    /**
     * Get account balance.
     */
    @GetMapping("/{accountId}/balance")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable @NonNull UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return ResponseEntity.ok(account.getBalance());
    }

    /**
     * Create a new account.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        AccountEntity account = AccountEntity.builder()
                .accountNumber("MT84AURU" + System.currentTimeMillis())
                .customerId(request.getCustomerId())
                .accountType(AccountEntity.AccountType.valueOf(request.getAccountType().toUpperCase()))
                .status(AccountEntity.AccountStatus.ACTIVE)
                .balance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO)
                .currency(request.getCurrency())
                .creditLimit(BigDecimal.ZERO)
                .openedAt(Instant.now())
                .build();

        AccountEntity saved = accountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    /**
     * Update account status.
     */
    @PutMapping("/{accountId}/status")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER')")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable @NonNull UUID accountId,
            @RequestParam String status) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        account.setStatus(AccountEntity.AccountStatus.valueOf(status.toUpperCase()));
        if (status.equalsIgnoreCase("CLOSED")) {
            account.setClosedAt(Instant.now());
        }
        
        AccountEntity updated = accountRepository.save(account);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * Delete an account.
     */
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<Void> deleteAccount(@PathVariable @NonNull UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        accountRepository.deleteById(accountId);
        return ResponseEntity.noContent().build();
    }

    private AccountResponse toResponse(AccountEntity entity) {
        return AccountResponse.builder()
                .id(entity.getId())
                .accountNumber(entity.getAccountNumber())
                .customerId(entity.getCustomerId())
                .accountType(entity.getAccountType().name())
                .status(entity.getStatus().name())
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .creditLimit(entity.getCreditLimit())
                .openedAt(entity.getOpenedAt())
                .closedAt(entity.getClosedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
