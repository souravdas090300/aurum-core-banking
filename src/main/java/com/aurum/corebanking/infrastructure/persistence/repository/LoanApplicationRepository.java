package com.aurum.corebanking.infrastructure.persistence.repository;

import com.aurum.corebanking.infrastructure.persistence.entity.LoanApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationRepository
        extends JpaRepository<LoanApplicationEntity, UUID> {

    List<LoanApplicationEntity> findByCustomerId(UUID customerId);

    Optional<LoanApplicationEntity> findByJbpmProcessId(String processId);

    List<LoanApplicationEntity> findByDecision(
            LoanApplicationEntity.LoanDecision decision);
}