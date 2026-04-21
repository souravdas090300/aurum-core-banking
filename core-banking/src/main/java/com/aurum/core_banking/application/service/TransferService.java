package com.aurum.core_banking.application.service;

import com.aurum.core_banking.common.audit.AuditService;
import com.aurum.core_banking.common.exception.AccountNotActiveException;
import com.aurum.core_banking.common.exception.AccountNotFoundException;
import com.aurum.core_banking.common.exception.CurrencyMismatchException;
import com.aurum.core_banking.common.exception.InsufficientFundsException;
import com.aurum.core_banking.common.exception.TransactionBlockedException;
import com.aurum.core_banking.domain.rules.CustomerFact;
import com.aurum.core_banking.domain.rules.TransactionFact;
import com.aurum.core_banking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.core_banking.infrastructure.persistence.entity.TransactionEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.core_banking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.core_banking.interfaces.rest.dto.request.TransferRequest;
import com.aurum.core_banking.interfaces.rest.dto.response.TransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository     accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService          auditService;
    private final FraudDetectionService  fraudDetectionService;

    // SERIALIZABLE isolation — highest level, prevents phantom reads in concurrent transfers
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public TransferResponse transfer(TransferRequest request, String performedBy) {

        // Idempotency check — return existing result if already processed
        return transactionRepository
            .findByIdempotencyKey(request.idempotencyKey())
            .map(this::toResponse)
            .orElseGet(() -> executeTransfer(request, performedBy));
    }

    private TransferResponse executeTransfer(TransferRequest req, String performedBy) {

        // Load accounts with pessimistic write lock to prevent concurrent balance updates
        AccountEntity from = accountRepository
            .findByIdForUpdate(req.fromAccountId())
            .orElseThrow(() -> new AccountNotFoundException(req.fromAccountId()));

        AccountEntity to = accountRepository
            .findByIdForUpdate(req.toAccountId())
            .orElseThrow(() -> new AccountNotFoundException(req.toAccountId()));

        validateTransfer(from, to, req);

        // ── Run Drools fraud evaluation BEFORE any money moves ──
        TransactionFact txFact = TransactionFact.builder()
            .transactionId(UUID.randomUUID())
            .fromAccountId(req.fromAccountId())
            .toAccountId(req.toAccountId())
            .amount(req.amount())
            .currency(req.currency())
            .timestamp(Instant.now())
            .build();

        CustomerFact customerFact = CustomerFact.builder()
            .customerId(from.getCustomerId().toString())
            .isPep(false) // load from customer repo in full impl
            .build();

        FraudResult fraud = fraudDetectionService.evaluate(txFact, customerFact);

        if (fraud.isBlocked()) {
            TransactionEntity blockedTx = TransactionEntity.builder()
                .idempotencyKey(req.idempotencyKey())
                .fromAccountId(req.fromAccountId())
                .toAccountId(req.toAccountId())
                .amount(req.amount())
                .currency(req.currency())
                .transactionType(TransactionEntity.TransactionType.TRANSFER)
                .status(TransactionEntity.TransactionStatus.BLOCKED)
                .reference(fraud.getBlockReason())
                .build();
            transactionRepository.save(blockedTx);

            throw new TransactionBlockedException(fraud.getBlockReason(), fraud.getFraudFlags());
        }

        // Debit source
        from.setBalance(from.getBalance().subtract(req.amount()));
        accountRepository.save(from);

        // Credit destination
        to.setBalance(to.getBalance().add(req.amount()));
        accountRepository.save(to);

        // Record transaction
        TransactionEntity tx = TransactionEntity.builder()
            .idempotencyKey(req.idempotencyKey())
            .fromAccountId(req.fromAccountId())
            .toAccountId(req.toAccountId())
            .amount(req.amount())
            .currency(req.currency())
            .transactionType(TransactionEntity.TransactionType.TRANSFER)
            .status(TransactionEntity.TransactionStatus.COMPLETED)
            .reference(req.reference())
            .executedAt(Instant.now())
            .build();
        transactionRepository.save(tx);

        auditService.log("TRANSACTION", tx.getId(), "TRANSFER_EXECUTED", performedBy, null, tx);

        log.info("Transfer completed: {} -> {} amount={}{}",
            req.fromAccountId(), req.toAccountId(), req.amount(), req.currency());

        return toResponse(tx);
    }

    private void validateTransfer(AccountEntity from, AccountEntity to, TransferRequest req) {
        if (from.getStatus() != AccountEntity.AccountStatus.ACTIVE)
            throw new AccountNotActiveException(from.getId());

        if (to.getStatus() != AccountEntity.AccountStatus.ACTIVE)
            throw new AccountNotActiveException(to.getId());

        if (from.getBalance().subtract(req.amount())
                .compareTo(from.getCreditLimit().negate()) < 0)
            throw new InsufficientFundsException(from.getId(), req.amount());

        if (!from.getCurrency().equals(req.currency()))
            throw new CurrencyMismatchException(req.currency(), from.getCurrency());
    }

    private TransferResponse toResponse(TransactionEntity tx) {
        return new TransferResponse(
            tx.getId(),
            tx.getStatus().name(),
            tx.getAmount(),
            tx.getCurrency(),
            tx.getReference(),
            tx.getExecutedAt()
        );
    }
}
