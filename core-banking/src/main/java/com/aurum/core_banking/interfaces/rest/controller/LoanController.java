package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.application.service.LoanProcessService;
import com.aurum.core_banking.infrastructure.persistence.entity.LoanEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.LoanRepository;
import com.aurum.core_banking.infrastructure.workflow.TaskSummaryItem;
import com.aurum.core_banking.interfaces.rest.dto.request.LoanApplicationRequest;
import com.aurum.core_banking.interfaces.rest.dto.request.LoanPaymentRequest;
import com.aurum.core_banking.interfaces.rest.dto.request.LoanReviewRequest;
import com.aurum.core_banking.interfaces.rest.dto.response.LoanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for loan application lifecycle management.
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class LoanController {

    private final LoanProcessService loanProcessService;
    private final LoanRepository loanRepository;

    /**
     * Get all loans.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER')")
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        List<LoanResponse> loans = loanRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loans);
    }

    /**
     * Get loan by ID.
     */
    @GetMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER')")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable @NonNull UUID loanId) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return ResponseEntity.ok(toResponse(loan));
    }

    /**
     * Submit a new loan application — triggers Drools credit scoring and starts the workflow.
     */
    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER')")
    public ResponseEntity<Map<String, Object>> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request) {

        Long processInstanceId = loanProcessService.submitLoanApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("processInstanceId", processInstanceId,
                             "message", "Loan application submitted successfully"));
    }

    /**
     * Make a payment on an existing loan.
     */
    @PostMapping("/{loanId}/payment")
    @PreAuthorize("hasRole('BANKING_USER')")
    public ResponseEntity<LoanResponse> makePayment(
            @PathVariable @NonNull UUID loanId,
            @Valid @RequestBody LoanPaymentRequest request) {
        
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Subtract payment from remaining balance
        BigDecimal newBalance = loan.getRemainingBalance().subtract(request.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        loan.setRemainingBalance(newBalance);

        // Update status if paid off
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanEntity.LoanStatus.PAID);
        }

        LoanEntity updated = loanRepository.save(loan);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * Loan officer approves or declines a manual-review task.
     */
    @PostMapping("/tasks/{taskId}/review")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<Void> reviewLoan(
            @PathVariable Long taskId,
            @Valid @RequestBody LoanReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String officerId = jwt != null ? jwt.getSubject() : "system";
        loanProcessService.completeLoanReview(taskId, officerId, request.approved(), request.notes());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve pending tasks for the authenticated loan officer.
     */
    @GetMapping("/tasks/queue")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<List<TaskSummaryItem>> getTaskQueue(
            @AuthenticationPrincipal Jwt jwt) {

        String officerId = jwt != null ? jwt.getSubject() : "system";
        List<TaskSummaryItem> tasks = loanProcessService.getOfficerTaskQueue(officerId);
        return ResponseEntity.ok(tasks);
    }

    private LoanResponse toResponse(LoanEntity entity) {
        return LoanResponse.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .accountId(entity.getAccountId())
                .loanType(entity.getLoanType().name())
                .principalAmount(entity.getPrincipalAmount())
                .remainingBalance(entity.getRemainingBalance())
                .interestRate(entity.getInterestRate())
                .termMonths(entity.getTermMonths())
                .monthlyPayment(entity.getMonthlyPayment())
                .nextPaymentDate(entity.getNextPaymentDate())
                .status(entity.getStatus().name())
                .approvedAt(entity.getApprovedAt())
                .disbursedAt(entity.getDisbursedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
