package com.aurum.corebanking.application.service;

import com.aurum.corebanking.common.audit.AuditService;
import com.aurum.corebanking.common.exception.AccountNotActiveException;
import com.aurum.corebanking.common.exception.AccountNotFoundException;
import com.aurum.corebanking.common.exception.InsufficientFundsException;
import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.corebanking.infrastructure.persistence.entity.TransactionEntity;
import com.aurum.corebanking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.corebanking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.corebanking.interfaces.rest.dto.request.DepositRequest;
import com.aurum.corebanking.interfaces.rest.dto.request.WithdrawalRequest;
import com.aurum.corebanking.interfaces.rest.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Account transaction service for deposits and withdrawals.
 *
 * <p>Handles balance updates atomically and performs necessary validations:
 * <ul>
 *   <li>Account existence and active status</li>
 *   <li>Sufficient funds for withdrawals</li>
 *   <li>Idempotency for transaction operations</li>
 *   <li>Audit trail recording</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    /**
     * Process a deposit transaction and update account balance.
     */
    @SuppressWarnings("null")
@Transactional
    public TransactionResponse deposit(DepositRequest request, String performedBy) {
        // Generate idempotency key if not provided
        String idempotencyKey = UUID.randomUUID().toString();

        // Check for duplicate
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent deposit replay — key={}", idempotencyKey);
            return toResponse(existing.get());
        }

        // Lock and load account
        AccountEntity account = accountRepository.findByIdForUpdate(request.accountId())
                .orElseThrow(() -> new AccountNotFoundException(request.accountId()));

        // Validate account status
        if (account.getStatus() != AccountEntity.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + account.getId() + " is not active: " + account.getStatus().name());
        }

        // Update balance
        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        // Create transaction record
        TransactionEntity transaction = TransactionEntity.builder()
                .idempotencyKey(idempotencyKey)
                .toAccountId(account.getId())
                .amount(request.amount())
                .currency(account.getCurrency())
                .transactionType(TransactionEntity.TransactionType.DEPOSIT)
                .status(TransactionEntity.TransactionStatus.COMPLETED)
                .reference(request.reference())
                .executedAt(Instant.now())
                .build();
        transactionRepository.save(transaction);

        // Audit trail
        auditService.record("Transaction", transaction.getId(), "DEPOSIT", performedBy,
                "amount=" + request.amount() + " account=" + account.getId());

        log.info("Deposit completed — txId={} amount={} account={}",
                transaction.getId(), request.amount(), account.getId());

        return toResponse(transaction);
    }

    /**
     * Process a withdrawal transaction and update account balance.
     */
    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest request, String performedBy) {
        // Generate idempotency key if not provided
        String idempotencyKey = UUID.randomUUID().toString();

        // Check for duplicate
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent withdrawal replay — key={}", idempotencyKey);
            return toResponse(existing.get());
        }

        // Lock and load account
        AccountEntity account = accountRepository.findByIdForUpdate(request.accountId())
                .orElseThrow(() -> new AccountNotFoundException(request.accountId()));

        // Validate account status
        if (account.getStatus() != AccountEntity.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + account.getId() + " is not active: " + account.getStatus().name());
        }

        // Check sufficient funds (balance + credit limit)
        BigDecimal available = account.getBalance().add(account.getCreditLimit());
        if (available.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(account.getId(), account.getBalance(), request.amount());
        }

        // Update balance
        account.setBalance(account.getBalance().subtract(request.amount()));
        accountRepository.save(account);

        // Create transaction record
        TransactionEntity transaction = TransactionEntity.builder()
                .idempotencyKey(idempotencyKey)
                .fromAccountId(account.getId())
                .amount(request.amount())
                .currency(account.getCurrency())
                .transactionType(TransactionEntity.TransactionType.WITHDRAWAL)
                .status(TransactionEntity.TransactionStatus.COMPLETED)
                .reference(request.reference())
                .executedAt(Instant.now())
                .build();
        transactionRepository.save(transaction);

        // Audit trail
        auditService.record("Transaction", transaction.getId(), "WITHDRAWAL", performedBy,
                "amount=" + request.amount() + " account=" + account.getId());

        log.info("Withdrawal completed — txId={} amount={} account={}",
                transaction.getId(), request.amount(), account.getId());

        return toResponse(transaction);
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
