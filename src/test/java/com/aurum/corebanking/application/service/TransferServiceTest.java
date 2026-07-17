package com.aurum.corebanking.application.service;

import com.aurum.corebanking.common.audit.AuditService;
import com.aurum.corebanking.common.exception.*;
import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity;
import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity.AccountStatus;
import com.aurum.corebanking.infrastructure.persistence.entity.TransactionEntity;
import com.aurum.corebanking.infrastructure.persistence.repository.AccountRepository;
import com.aurum.corebanking.infrastructure.persistence.repository.TransactionRepository;
import com.aurum.corebanking.interfaces.rest.dto.request.TransferRequest;
import com.aurum.corebanking.interfaces.rest.dto.response.TransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TransferService}.
 *
 * <p>All external dependencies (repositories, fraud service, audit) are mocked.
 * Tests verify the orchestration logic without hitting a real database or Drools.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService unit tests")
class TransferServiceTest {

    @Mock AccountRepository     accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock FraudDetectionService fraudDetectionService;
    @Mock AuditService          auditService;

    @InjectMocks
    TransferService transferService;

    private AccountEntity debitAccount;
    private AccountEntity creditAccount;
    private FraudResult   cleanFraudResult;

    @BeforeEach
    void setUp() {
        debitAccount = AccountEntity.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .accountNumber("MT84MALT011000012345MTLCAST001S")
                .customerId(UUID.randomUUID())
                .accountType(AccountEntity.AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("5000.00"))
                .currency("EUR")
                .creditLimit(BigDecimal.ZERO)
                .build();

        creditAccount = AccountEntity.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                .accountNumber("MT84MALT011000099999MTLCAST001S")
                .customerId(UUID.randomUUID())
                .accountType(AccountEntity.AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .currency("EUR")
                .creditLimit(BigDecimal.ZERO)
                .build();

        cleanFraudResult = FraudResult.builder()
                .blocked(false)
                .fraudFlags(List.of())
                .fiauReportRequired(false)
                .amlAlert(false)
                .build();
    }

    @Test
    @DisplayName("Successful transfer debits sender and credits receiver")
    void successfulTransfer_debitsAndCredits() {
        TransferRequest request = new TransferRequest(
                debitAccount.getId(), creditAccount.getId(),
                new BigDecimal("500.00"), "EUR", "Rent payment", "idempotency-key-001");

        when(transactionRepository.findByIdempotencyKey("idempotency-key-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(debitAccount.getId()))
                .thenReturn(Optional.of(debitAccount));
        when(accountRepository.findByIdForUpdate(creditAccount.getId()))
                .thenReturn(Optional.of(creditAccount));
        when(fraudDetectionService.evaluate(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cleanFraudResult);
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> {
                    TransactionEntity tx = inv.getArgument(0);
                    // simulate DB-assigned ID
                    try {
                        var f = TransactionEntity.class.getDeclaredField("id");
                        f.setAccessible(true);
                        f.set(tx, UUID.randomUUID());
                        var ea = TransactionEntity.class.getDeclaredField("executedAt");
                        ea.setAccessible(true);
                        ea.set(tx, Instant.now());
                    } catch (Exception ignored) {}
                    return tx;
                });

        TransferResponse response = transferService.execute(request, "user-123");

        assertThat(debitAccount.getBalance()).isEqualByComparingTo("4500.00");
        assertThat(creditAccount.getBalance()).isEqualByComparingTo("1500.00");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getAmount()).isEqualByComparingTo("500.00");

        verify(auditService).record(eq("Transaction"), any(), eq("TRANSFER"), eq("user-123"), any());
    }

    @Test
    @DisplayName("Duplicate idempotency key returns original transaction without re-executing")
    void duplicateIdempotencyKey_returnsOriginalWithoutReexecuting() {
        TransactionEntity existing = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .idempotencyKey("idempotency-key-dup")
                .fromAccountId(debitAccount.getId())
                .toAccountId(creditAccount.getId())
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .transactionType(TransactionEntity.TransactionType.TRANSFER)
                .status(TransactionEntity.TransactionStatus.COMPLETED)
                .executedAt(Instant.now())
                .build();

        when(transactionRepository.findByIdempotencyKey("idempotency-key-dup"))
                .thenReturn(Optional.of(existing));

        TransferRequest request = new TransferRequest(
                debitAccount.getId(), creditAccount.getId(),
                new BigDecimal("100.00"), "EUR", "Test", "idempotency-key-dup");

        TransferResponse response = transferService.execute(request, "user-123");

        assertThat(response.getTransactionId()).isEqualTo(existing.getId());
        verifyNoInteractions(accountRepository, fraudDetectionService, auditService);
    }

    @Test
    @DisplayName("Insufficient funds throws InsufficientFundsException")
    void insufficientFunds_throwsException() {
        TransferRequest request = new TransferRequest(
                debitAccount.getId(), creditAccount.getId(),
                new BigDecimal("99999.00"), "EUR", "Huge transfer", "idempotency-key-003");

        when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(debitAccount.getId()))
                .thenReturn(Optional.of(debitAccount));
        when(accountRepository.findByIdForUpdate(creditAccount.getId()))
                .thenReturn(Optional.of(creditAccount));

        assertThatThrownBy(() -> transferService.execute(request, "user-123"))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    @DisplayName("Frozen account throws AccountNotActiveException")
    void frozenAccount_throwsException() {
        debitAccount.setStatus(AccountStatus.FROZEN);

        TransferRequest request = new TransferRequest(
                debitAccount.getId(), creditAccount.getId(),
                new BigDecimal("100.00"), "EUR", "Test", "idempotency-key-004");

        when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(debitAccount.getId()))
                .thenReturn(Optional.of(debitAccount));
        when(accountRepository.findByIdForUpdate(creditAccount.getId()))
                .thenReturn(Optional.of(creditAccount));

        assertThatThrownBy(() -> transferService.execute(request, "user-123"))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("Fraud-blocked transaction throws TransactionBlockedException")
    void fraudBlocked_throwsException() {
        FraudResult blockedResult = FraudResult.builder()
                .blocked(true)
                .blockReason("SANCTIONS_MATCH")
                .fraudFlags(List.of("SANCTIONS_MATCH"))
                .fiauReportRequired(false)
                .amlAlert(true)
                .build();

        TransferRequest request = new TransferRequest(
                debitAccount.getId(), creditAccount.getId(),
                new BigDecimal("500.00"), "EUR", "Blocked", "idempotency-key-005");

        when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(debitAccount.getId()))
                .thenReturn(Optional.of(debitAccount));
        when(accountRepository.findByIdForUpdate(creditAccount.getId()))
                .thenReturn(Optional.of(creditAccount));
        when(fraudDetectionService.evaluate(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(blockedResult);

        assertThatThrownBy(() -> transferService.execute(request, "user-123"))
                .isInstanceOf(TransactionBlockedException.class)
                .hasMessageContaining("SANCTIONS_MATCH");
    }
}
