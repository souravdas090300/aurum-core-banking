-- ============================================================
-- Aurum Core Banking — Audit Log Table
-- V2__audit_log.sql
-- ============================================================

-- AUDIT_LOG table — immutable compliance audit trail
-- Retention: 7 years (Malta PMLA / MFSA requirement)
-- Never update or delete rows from this table.
CREATE TABLE audit_log (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type     VARCHAR(100)    NOT NULL,        -- e.g. "TRANSACTION", "ACCOUNT"
    entity_id       UUID,                            -- UUID of the affected entity
    action          VARCHAR(100)    NOT NULL,        -- e.g. "TRANSFER_EXECUTED", "GDPR_ERASURE_PROCESSED"
    performed_by    VARCHAR(255)    NOT NULL,        -- username, Keycloak sub, or "SYSTEM"
    old_value       TEXT,                            -- previous state (JSON)
    new_value       TEXT,                            -- new state (JSON)
    ip_address      VARCHAR(45),                     -- IPv4 or IPv6 (incl. X-Forwarded-For)
    trace_id        VARCHAR(64),                     -- MDC correlation ID
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Indexes for audit query patterns
CREATE INDEX idx_audit_entity     ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_by         ON audit_log(performed_by);
CREATE INDEX idx_audit_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_trace_id   ON audit_log(trace_id);

-- Prevent modifications to existing audit rows
-- (Row-level security on write is enforced at application level + DB role)
