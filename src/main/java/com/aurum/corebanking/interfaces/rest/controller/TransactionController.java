package com.aurum.corebanking.interfaces.rest.controller;

import com.aurum.corebanking.application.service.AccountService;
import com.aurum.corebanking.application.service.TransferService;
import com.aurum.corebanking.infrastructure.persistence.entity.TransactionEntity;
import com.aurum.corebanking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.corebanking.interfaces.rest.dto.request.TransferRequest;
import com.aurum.corebanking.interfaces.rest.dto.request.DepositRequest;
import com.aurum.corebanking.interfaces.rest.dto.request.WithdrawalRequest;
import com.aurum.corebanking.interfaces.rest.dto.response.TransactionResponse;
import com.aurum.corebanking.interfaces.rest.dto.response.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for transaction operations.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransferService transferService;
    private final AccountService accountService;

    /**
     * Get all transactions with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','MANAGER','AUDITOR','ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TransactionResponse> transactions = transactionRepository.findAll(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by account ID.
     */
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','MANAGER','AUDITOR','ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable UUID accountId) {
        List<TransactionResponse> transactions = transactionRepository
                .findByFromAccountIdOrToAccountId(accountId, accountId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transaction by ID.
     */
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','MANAGER','AUDITOR','ADMIN')")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable @NonNull UUID transactionId) {
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return ResponseEntity.ok(toResponse(transaction));
    }

    /**
     * Execute a transfer.
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','ADMIN')")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        // Extract username from JWT token or default to "system" for dev mode
        String performedBy = extractUsername();
        TransferResponse response = transferService.execute(request, performedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Deposit money into an account.
     */
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','ADMIN')")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        String performedBy = extractUsername();
        TransactionResponse response = accountService.deposit(request, performedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraw money from an account.
     */
    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','ADMIN')")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        String performedBy = extractUsername();
        TransactionResponse response = accountService.withdraw(request, performedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Extract username from JWT token in SecurityContext.
     * Falls back to "system" if no authentication is present (e.g., in dev mode).
     */
    private String extractUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }
        return "system";
    }

    private TransactionResponse toResponse(TransactionEntity entity) {
        // Determine accountId based on transaction type
        UUID accountId = entity.getFromAccountId() != null 
                ? entity.getFromAccountId() 
                : entity.getToAccountId();
        
        return new TransactionResponse(
                entity.getId(),
                entity.getTransactionType().name(),
                accountId,
                entity.getAmount(),
                entity.getCurrency(),
                entity.getReference(),
                entity.getStatus().name(),
                entity.getExecutedAt()
        );
    }
}
