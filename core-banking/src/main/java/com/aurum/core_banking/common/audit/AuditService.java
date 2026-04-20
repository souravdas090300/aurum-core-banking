package com.aurum.core_banking.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AuditService {

    /**
     * Records an audit event. In Phase 3 this will persist to the audit_log table.
     *
     * @param entityType  e.g. "TRANSACTION", "ACCOUNT"
     * @param entityId    the UUID of the affected entity
     * @param action      e.g. "TRANSFER_EXECUTED", "ACCOUNT_FROZEN"
     * @param performedBy the authenticated username or "SYSTEM"
     * @param oldValue    previous state (nullable)
     * @param newValue    new state (nullable)
     */
    public void log(String entityType, UUID entityId, String action,
                    String performedBy, Object oldValue, Object newValue) {
        log.info("[AUDIT] entity={} id={} action={} by={}", entityType, entityId, action, performedBy);
    }
}
