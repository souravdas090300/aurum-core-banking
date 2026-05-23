package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.core_banking.infrastructure.persistence.entity.TransactionEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.core_banking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.core_banking.interfaces.rest.dto.response.AccountResponse;
import com.aurum.core_banking.interfaces.rest.dto.response.DashboardStatsResponse;
import com.aurum.core_banking.interfaces.rest.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for dashboard statistics.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DashboardController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Get dashboard statistics.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        List<AccountEntity> accounts = accountRepository.findAll();
        
        BigDecimal totalBalance = accounts.stream()
                .map(AccountEntity::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant oneMonthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<TransactionEntity> recentTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(oneMonthAgo))
                .collect(Collectors.toList());

        BigDecimal monthlyIncome = recentTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionEntity.TransactionType.DEPOSIT)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlySpending = recentTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionEntity.TransactionType.WITHDRAWAL ||
                           t.getTransactionType() == TransactionEntity.TransactionType.TRANSFER)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .totalBalance(totalBalance)
                .totalAccounts(accounts.size())
                .monthlyIncome(monthlyIncome)
                .monthlySpending(monthlySpending)
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent transactions.
     */
    @GetMapping("/recent-transactions")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<List<TransactionResponse>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit) {
        List<TransactionResponse> transactions = transactionRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(limit)
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    /**
     * Get account summary.
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AccountResponse>> getAccountsSummary() {
        List<AccountResponse> accounts = accountRepository.findAll().stream()
                .filter(a -> a.getStatus() == AccountEntity.AccountStatus.ACTIVE)
                .map(this::toAccountResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(accounts);
    }

    private TransactionResponse toTransactionResponse(TransactionEntity entity) {
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

    private AccountResponse toAccountResponse(AccountEntity entity) {
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
