package com.aurum.core_banking.infrastructure.persistence.repository;

import com.aurum.core_banking.infrastructure.persistence.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, UUID> {
    List<LoanEntity> findByCustomerId(UUID customerId);
    List<LoanEntity> findByStatus(LoanEntity.LoanStatus status);
}
