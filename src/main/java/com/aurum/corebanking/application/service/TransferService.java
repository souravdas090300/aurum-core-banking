package com.aurum.corebanking.application.service;

import com.aurum.corebanking.common.audit.AuditService;
import com.aurum.corebanking.common.exception.*;
import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.corebanking.infrastructure.persistence.entity.TransactionEntity;
import com.aurum.corebanking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.corebanking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.corebanking.interfaces.rest.dto.request.TransferRequest;
import com.aurum.corebanking.interfaces.rest.dto.response.TransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Core transfer orchestration service.
 *
 * <p>Applies the following safeguards on every transfer:
 * <ol>
 *   <li>Idempotency — rejects duplicate {@code idempotencyKey} values.</li>
 *   <li>Account validation — both accounts must exist and be ACTIVE.</li>
 *   <li>Currency match — debit and credit accounts must use the same currency.</li>
 *   <li>Sufficient funds — debit account must have adequate balance (including credit limit).</li>
 *   <li>Fraud &amp; AML screening — all transfers run through Drools rules.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository     accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AuditService          auditService;

    @Transactional
    public TransferResponse execute(TransferRequest request, String performedBy) {

        // 1. Idempotency check
        var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Idempotent transfer replay — key={}", request.idempotencyKey());
            return toResponse(existing.get());
        }

        // 2. Lock and load both accounts
        AccountEntity debit  = accountRepository.findByIdForUpdate(request.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));
        AccountEntity credit = accountRepository.findByIdForUpdate(request.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));

        // 3. Status checks
        if (debit.getStatus() != AccountEntity.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + debit.getId() + " is not active: " + debit.getStatus().name());
        }
        if (credit.getStatus() != AccountEntity.AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + credit.getId() + " is not active: " + credit.getStatus().name());
        }

        // 4. Currency match
        if (!debit.getCurrency().equalsIgnoreCase(request.currency()) ||
            !credit.getCurrency().equalsIgnoreCase(request.currency())) {
            throw new CurrencyMismatchException("Currency mismatch: account currency=" + debit.getCurrency() + ", request currency=" + request.currency());
        }

        // 5. Sufficient funds (balance + creditLimit must cover the amount)
        var available = debit.getBalance().add(debit.getCreditLimit());
        if (available.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(debit.getId(), debit.getBalance(), request.amount());
        }

        // 6. Fraud & AML screening
        FraudResult fraud = fraudDetectionService.evaluate(
                UUID.randomUUID(), debit.getId(), credit.getId(),
                request.amount(), request.currency(),
                "MT", "MT",   // country codes — extend with customer profile lookup
                performedBy);

        if (fraud.isBlocked()) {
            throw new TransactionBlockedException(fraud.getBlockReason());
        }

        // 7. Apply balance changes
        debit.setBalance(debit.getBalance().subtract(request.amount()));
        credit.setBalance(credit.getBalance().add(request.amount()));
        accountRepository.save(debit);
        accountRepository.save(credit);

        // 8. Persist transaction record
        TransactionEntity tx = TransactionEntity.builder()
                .idempotencyKey(request.idempotencyKey())
                .fromAccountId(debit.getId())
                .toAccountId(credit.getId())
                .amount(request.amount())
                .currency(request.currency())
                .transactionType(TransactionEntity.TransactionType.TRANSFER)
                .status(TransactionEntity.TransactionStatus.COMPLETED)
                .reference(request.reference())
                .fraudFlag(fraud.getFraudFlags().isEmpty() ? null : fraud.getFraudFlags().get(0))
                .executedAt(Instant.now())
                .build();
        transactionRepository.save(tx);

        // 9. Audit trail
        auditService.record("Transaction", tx.getId(), "TRANSFER", performedBy,
                "amount=" + request.amount() + " currency=" + request.currency()
                        + " from=" + debit.getId() + " to=" + credit.getId()
                        + " flags=" + fraud.getFraudFlags());

        log.info("Transfer completed — txId={} amount={} {} from={} to={}",
                tx.getId(), request.amount(), request.currency(),
                debit.getId(), credit.getId());

        return toResponse(tx);
    }

    private TransferResponse toResponse(TransactionEntity tx) {
        return TransferResponse.builder()
                .transactionId(tx.getId())
                .status(tx.getStatus().name())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .reference(tx.getReference())
                .executedAt(tx.getExecutedAt())
                .build();
    }
}
