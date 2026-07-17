package com.aurum.corebanking.infrastructure.persistence.repository;

import com.aurum.corebanking.infrastructure.persistence.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AccountEntity with pessimistic locking support.
 * Handles account CRUD operations and balance updates with concurrency control.
 */
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    /**
     * Find an account by its IBAN account number.
     *
     * @param accountNumber the IBAN account number
     * @return Optional containing the account if found
     */
    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts belonging to a specific customer.
     *
     * @param customerId the customer's UUID
     * @return list of accounts for the customer
     */
    List<AccountEntity> findByCustomerId(UUID customerId);

    /**
     * Check if a customer has any account with a specific status.
     * Used by GDPR service to verify if customer has active accounts before erasure.
     *
     * @param customerId the customer's UUID
     * @param status the account status (ACTIVE, FROZEN, CLOSED)
     * @return true if at least one account with the given status exists
     */
    boolean existsByCustomerIdAndStatus(UUID customerId, String status);

    /**
     * Find an account by ID with a pessimistic write lock.
     * Prevents concurrent balance updates on the same row.
     * Must be called within a @Transactional method.
     *
     * @param id the account UUID
     * @return Optional containing the locked account if found
     */
  
    // Add this method to your existing AccountRepository interface
         @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT a FROM AccountEntity a WHERE a.id = :id")
        Optional<AccountEntity> findByIdForUpdate(@Param("id") UUID id);
    }