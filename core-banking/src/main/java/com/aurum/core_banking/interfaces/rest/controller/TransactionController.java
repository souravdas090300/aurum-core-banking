package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.application.service.TransferService;
import com.aurum.core_banking.infrastructure.persistence.entity.TransactionEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.core_banking.interfaces.rest.dto.request.TransferRequest;
import com.aurum.core_banking.interfaces.rest.dto.request.DepositRequest;
import com.aurum.core_banking.interfaces.rest.dto.request.WithdrawalRequest;
import com.aurum.core_banking.interfaces.rest.dto.response.TransactionResponse;
import com.aurum.core_banking.interfaces.rest.dto.response.TransferResponse;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransferService transferService;

    /**
     * Get all transactions with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
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
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable @NonNull UUID accountId) {
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
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable @NonNull UUID transactionId) {
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return ResponseEntity.ok(toResponse(transaction));
    }

    /**
     * Execute a transfer.
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('BANKING_USER')")
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
    @PreAuthorize("hasRole('BANKING_USER')")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        // Create a deposit transaction
        TransactionEntity transaction = TransactionEntity.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .toAccountId(request.getAccountId())
                .amount(request.getAmount())
                .currency("EUR")
                .transactionType(TransactionEntity.TransactionType.DEPOSIT)
                .status(TransactionEntity.TransactionStatus.COMPLETED)
                .reference(request.getReference())
                .build();

        TransactionEntity saved = transactionRepository.save(transaction);
        return ResponseEntity.ok(toResponse(saved));
    }

    /**
     * Withdraw money from an account.
     */
    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('BANKING_USER')")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        // Create a withdrawal transaction
        TransactionEntity transaction = TransactionEntity.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .fromAccountId(request.getAccountId())
                .amount(request.getAmount())
                .currency("EUR")
                .transactionType(TransactionEntity.TransactionType.WITHDRAWAL)
                .status(TransactionEntity.TransactionStatus.COMPLETED)
                .reference(request.getReference())
                .build();

        TransactionEntity saved = transactionRepository.save(transaction);
        return ResponseEntity.ok(toResponse(saved));
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
        return TransactionResponse.builder()
                .id(entity.getId())
                .idempotencyKey(entity.getIdempotencyKey())
                .fromAccountId(entity.getFromAccountId())
                .toAccountId(entity.getToAccountId())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .transactionType(entity.getTransactionType().name())
                .status(entity.getStatus().name())
                .reference(entity.getReference())
                .fraudFlag(entity.getFraudFlag())
                .executedAt(entity.getExecutedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
