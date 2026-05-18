package com.aurum.core_banking.common.audit;

import com.aurum.core_banking.infrastructure.persistence.entity.AuditLogEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.AuditLogRepository;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Compliance-grade audit service.
 *
 * Every state-changing operation produces an audit event that is:
 *  1. Persisted to the {@code audit_log} PostgreSQL table (queryable, indexed)
 *  2. Written to {@code audit.log} via the "AUDIT" logger (7-year file retention)
 *
 * Writes are @Async so they never add latency to the main transaction.
 * Audit failures are logged but never propagated — they must not break business flows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    /** Routes to audit.log with 7-year file retention in prod (logback-spring.xml). */
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper        objectMapper;

    /**
     * Record an action performed on a domain entity.
     *
     * @param entityType  entity class name (e.g. "Account", "Transaction")
     * @param entityId    the UUID of the entity
     * @param action      verb describing what happened (e.g. "TRANSFER", "FREEZE")
     * @param performedBy user ID or system principal
     * @param details     free-text description or JSON payload
     */
    @Async
    public void record(String entityType, UUID entityId, String action,
                       String performedBy, String details) {
        writeAuditEvent(entityType, entityId, action, performedBy, null, details);
    }

    /**
     * Record an audit event with old/new value objects (serialised to JSON).
     * Used by GDPR erasure, compliance services.
     *
     * @param entityType  e.g. "TRANSACTION", "ACCOUNT", "CUSTOMER"
     * @param entityId    UUID of the affected entity (nullable for system events)
     * @param action      e.g. "TRANSFER_EXECUTED", "GDPR_ERASURE_PROCESSED"
     * @param performedBy authenticated username, Keycloak subject, or "SYSTEM"
     * @param oldValue    previous state (nullable — serialised to JSON)
     * @param newValue    new state (nullable — serialised to JSON)
     */
    @Async
    public void log(String entityType, UUID entityId, String action,
                    String performedBy, Object oldValue, Object newValue) {
        try {
            String oldJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
            String newJson = newValue != null ? objectMapper.writeValueAsString(newValue) : null;
            writeAuditEvent(entityType, entityId, action, performedBy, oldJson, newJson);
        } catch (Exception e) {
            log.error("CRITICAL: Audit log write failed — entity={} id={} action={}",
                    entityType, entityId, action, e);
        }
    }

    /**
     * Convenience overload for system-initiated events.
     */
    @Async
    public void recordSystem(String entityType, UUID entityId, String action, String details) {
        record(entityType, entityId, action, "SYSTEM", details);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void writeAuditEvent(String entityType, UUID entityId, String action,
                                  String performedBy, String oldJson, String newJson) {
        try {
            AuditLogEntity entry = AuditLogEntity.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .performedBy(performedBy)
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .ipAddress(MDC.get("clientIp"))
                    .traceId(MDC.get("traceId"))
                    .build();
            auditLogRepository.save(entry);

            AUDIT_LOG.info("entity={} id={} action={} by={} traceId={}",
                    entityType, entityId, action, performedBy, MDC.get("traceId"));

        } catch (Exception e) {
            log.error("CRITICAL: Audit log write failed — entity={} id={} action={}",
                    entityType, entityId, action, e);
        }
    }
}
