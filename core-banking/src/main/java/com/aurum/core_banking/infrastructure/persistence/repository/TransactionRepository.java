package com.aurum.core_banking.infrastructure.persistence.repository;

import com.aurum.core_banking.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find transactions by account ID (either from or to).
     */
    List<TransactionEntity> findByFromAccountIdOrToAccountId(UUID fromAccountId, UUID toAccountId);

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
     * FIAU reporting: transactions flagged with a specific fraud flag
     * within a time window.
     */
    @Query("SELECT t FROM TransactionEntity t " +
           "WHERE t.fraudFlag = :fraudFlag " +
           "AND t.createdAt BETWEEN :from AND :to")
    List<TransactionEntity> findByFraudFlagAndCreatedAtBetween(
            @Param("fraudFlag") String fraudFlag,
            @Param("from") Instant from,
            @Param("to") Instant to);

    /**
     * GDPR: find the most recent transaction involving an account owner.
     */
    @Query("SELECT t FROM TransactionEntity t " +
           "WHERE t.fromAccountId IN (SELECT a.id FROM AccountEntity a WHERE a.customerId = :customerId) " +
           "OR t.toAccountId IN (SELECT a.id FROM AccountEntity a WHERE a.customerId = :customerId) " +
           "ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByCustomerId(@Param("customerId") UUID customerId);

    /**
     * GDPR: find the latest transaction touching any account of a customer.
     */
    @Query("SELECT t FROM TransactionEntity t " +
           "WHERE (t.fromAccountId IN (SELECT a.id FROM AccountEntity a WHERE a.customerId = :customerId) " +
           "OR t.toAccountId IN (SELECT a.id FROM AccountEntity a WHERE a.customerId = :customerId)) " +
           "ORDER BY t.createdAt DESC LIMIT 1")
    Optional<TransactionEntity> findLatestByCustomerId(@Param("customerId") UUID customerId);
}
