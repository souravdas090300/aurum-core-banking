package com.aurum.corebanking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_id")
    private UUID accountId;

    // BigDecimal — never double for money
    @Column(name = "requested_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal requestedAmount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(length = 255)
    private String purpose;

    @Column(name = "credit_score")
    private Integer creditScore;

    // Set by Drools credit-scoring.drl: APPROVED / REVIEW / DECLINED
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LoanDecision decision;

    // jBPM process instance ID for workflow tracking
    @Column(name = "jbpm_process_id", length = 100)
    private String jbpmProcessId;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public enum LoanDecision {
        APPROVED, REVIEW, DECLINED
    }
}