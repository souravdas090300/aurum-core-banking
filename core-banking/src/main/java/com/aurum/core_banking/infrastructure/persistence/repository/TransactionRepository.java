package com.aurum.core_banking.infrastructure.persistence.repository;

import com.aurum.core_banking.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);

    long countByFromAccountIdAndCreatedAtAfter(UUID fromAccountId, Instant after);
}
