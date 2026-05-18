package com.aurum.core_banking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit log record.
 * Written for every state-changing operation.
 * Retained for 7 years per Malta banking regulations (PMLA / MFSA).
 */
@Entity
@Table(
    name = "audit_log",
    indexes = {
        @Index(name = "idx_audit_entity",     columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_created_at",   columnList = "created_at"),
        @Index(name = "idx_audit_trace_id",     columnList = "trace_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
