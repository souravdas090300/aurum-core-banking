package com.aurum.corebanking.infrastructure.persistence.repository;

import com.aurum.corebanking.infrastructure.persistence.entity.TransactionEntity;
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
           "WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId " +
           "ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByFromAccountIdOrToAccountId(
            @Param("accountId") UUID accountId,
            @Param("accountId") UUID accountId2);
}
