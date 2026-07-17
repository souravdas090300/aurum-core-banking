package com.aurum.corebanking.infrastructure.persistence.repository;

import com.aurum.corebanking.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<CustomerEntity> findByCountryCode(String countryCode);

    // Used by GdprComplianceService retention job
    @Query("SELECT c FROM CustomerEntity c WHERE c.updatedAt < :cutoff " +
           "AND c.firstName <> 'ANONYMISED'")
    List<CustomerEntity> findEligibleForAnonymisation(@Param("cutoff") Instant cutoff);
}