package com.aurum.core_banking.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Immutable audit trail service.
 *
 * Logs significant domain events (account creation, transfers, decisions) so that
 * every state change is traceable for regulatory compliance.
 *
 * In production this would persist to the {@code audit_log} table. For now it
 * emits structured log events that can be harvested by a SIEM or log pipeline.
 */
@Slf4j
@Service
public class AuditService {

    /**
     * Record an action performed on a domain entity.
     *
     * @param entityType  entity class name (e.g. "Account", "Transaction")
     * @param entityId    the UUID of the entity
     * @param action      verb describing what happened (e.g. "TRANSFER", "FREEZE")
     * @param performedBy user ID or system principal
     * @param details     free-text description or JSON payload
     */
    public void record(String entityType, UUID entityId, String action,
                       String performedBy, String details) {
        log.info("[AUDIT] entity={} id={} action={} by={} details={}",
                 entityType, entityId, action, performedBy, details);
    }

    /**
     * Convenience overload for system-initiated events.
     */
    public void recordSystem(String entityType, UUID entityId, String action, String details) {
        record(entityType, entityId, action, "SYSTEM", details);
    }
}
