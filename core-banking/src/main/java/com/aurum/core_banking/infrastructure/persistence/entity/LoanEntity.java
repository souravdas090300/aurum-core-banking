package com.aurum.core_banking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loans")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEntity {

    public enum LoanStatus { PENDING, APPROVED, ACTIVE, PAID, DEFAULTED }
    public enum LoanType { PERSONAL, MORTGAGE, AUTO, BUSINESS }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_id")
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 30)
    private LoanType loanType;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "remaining_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingBalance;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "monthly_payment", precision = 19, scale = 4)
    private BigDecimal monthlyPayment;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "disbursed_at")
    private Instant disbursedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
