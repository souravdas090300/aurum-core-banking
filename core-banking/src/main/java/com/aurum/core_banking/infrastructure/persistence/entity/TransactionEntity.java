package com.aurum.core_banking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ✅ Idempotency key — prevents duplicate transfers (PSD2 requirement)
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "from_account_id")
    private UUID fromAccountId;

    @Column(name = "to_account_id")
    private UUID toAccountId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 140)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_flag")
    private FraudFlag fraudFlag;

    @Column(name = "executed_at")
    private Instant executedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public enum TransactionType  { TRANSFER, DEPOSIT, WITHDRAWAL, FEE }
    public enum TransactionStatus { PENDING, COMPLETED, FAILED, BLOCKED }
    public enum FraudFlag { FIAU_REPORT_REQUIRED, STRUCTURING_SUSPECTED,
                            SANCTIONS_MATCH, VELOCITY_BREACH, CLEAR }
}