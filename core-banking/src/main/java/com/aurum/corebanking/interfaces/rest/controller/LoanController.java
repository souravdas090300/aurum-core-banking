package com.aurum.corebanking.interfaces.rest.controller;

import com.aurum.corebanking.application.service.LoanProcessService;
import com.aurum.corebanking.infrastructure.workflow.TaskSummaryItem;
import com.aurum.corebanking.interfaces.rest.dto.request.LoanApplicationRequest;
import com.aurum.corebanking.interfaces.rest.dto.request.LoanReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for loan application lifecycle management.
 *
 * <p>Orchestrates credit scoring (Drools) and workflow (in-memory jBPM stub).
 */
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanProcessService loanProcessService;

    /**
     * Submit a new loan application — triggers Drools credit scoring and starts the
     * workflow process instance.
     */
    @PostMapping("/applications")
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
}
