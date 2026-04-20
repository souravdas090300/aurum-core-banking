package com.aurum.core_banking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    // ✅ CRITICAL: BigDecimal for money — NEVER double or float
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal balance;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(name = "credit_limit", precision = 19, scale = 4, nullable = false)
    private BigDecimal creditLimit;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ✅ Optimistic locking — prevents concurrent modification bugs
    @Version
    private Long version;

    public enum AccountType  { CURRENT, SAVINGS, LOAN }
    public enum AccountStatus { ACTIVE, FROZEN, CLOSED, PENDING }
}