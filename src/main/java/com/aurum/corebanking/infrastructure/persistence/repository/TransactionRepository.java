package com.aurum.corebanking.infrastructure.persistence.repository;

import com.aurum.corebanking.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Count transactions initiated from an account after a given timestamp.
     * Used by fraud detection to compute velocity (transactions per hour).
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t " +
           "WHERE t.fromAccountId = :accountId AND t.createdAt > :since")
    int countByFromAccountIdAndCreatedAtAfter(
            @Param("accountId") UUID accountId,
            @Param("since") Instant since);

    /**
     * Find all transactions involving a specific account (either as sender or receiver).
     */
    @Query("SELECT t FROM TransactionEntity t " +
           "WHERE t.fromAccountId = :accountId1 OR t.toAccountId = :accountId2 " +
           "ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByFromAccountIdOrToAccountId(
            @Param("accountId1") UUID accountId1,
            @Param("accountId2") UUID accountId2);

    /**
     * Find the latest transaction for a given customer (via their accounts).
     * Used by GDPR service to check retention period.
     */
    @Query("SELECT t FROM TransactionEntity t " +
           "WHERE t.fromAccountId IN (SELECT a.id FROM AccountEntity a WHERE a.customerId = :customerId) " +
           "   OR t.toAccountId IN (SELECT a.id FROM AccountEntity a WHERE a.customerId = :customerId) " +
           "ORDER BY t.createdAt DESC")
    Optional<TransactionEntity> findLatestByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Find transactions flagged for FIAU reporting within a time range.
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.fraudFlag = :flag AND t.createdAt BETWEEN :from AND :to")
    List<TransactionEntity> findByFraudFlagAndCreatedAtBetween(
            @Param("flag") String flag,
            @Param("from") Instant from,
            @Param("to") Instant to);
}