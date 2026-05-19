package com.aurum.core_banking.infrastructure.persistence.repository;

import com.aurum.core_banking.infrastructure.persistence.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    /**
     * Pessimistic write lock — prevents concurrent balance updates on the same row.
     * Called before debit/credit operations inside a transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.id = :id")
    Optional<AccountEntity> findByIdForUpdate(@Param("id") UUID id);

    /** GDPR: all accounts belonging to a customer. */
    List<AccountEntity> findByCustomerId(UUID customerId);

    /**
     * GDPR erasure check: does the customer have any ACTIVE accounts?
     * Banks cannot erase customers with open accounts.
     */
    @Query("SELECT COUNT(a) > 0 FROM AccountEntity a " +
           "WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    boolean existsByCustomerIdAndStatusActive(@Param("customerId") UUID customerId);

    /**
     * GDPR retention job: find closed accounts not updated since a cutoff date.
     */
    List<AccountEntity> findByStatusAndUpdatedAtBefore(
            AccountEntity.AccountStatus status, java.time.Instant cutoff);
}
