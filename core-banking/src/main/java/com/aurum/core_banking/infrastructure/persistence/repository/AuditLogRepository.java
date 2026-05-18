package com.aurum.core_banking.infrastructure.persistence.repository;

import com.aurum.core_banking.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    List<AuditLogEntity> findByEntityId(UUID entityId);

    List<AuditLogEntity> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<AuditLogEntity> findByPerformedBy(String performedBy);
}
