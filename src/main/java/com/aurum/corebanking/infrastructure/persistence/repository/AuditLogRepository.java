package com.aurum.corebanking.infrastructure.persistence.repository;

import com.aurum.corebanking.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
    Page<AuditLogEntity> findByEntityType(String entityType, Pageable pageable);
    Page<AuditLogEntity> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);
}