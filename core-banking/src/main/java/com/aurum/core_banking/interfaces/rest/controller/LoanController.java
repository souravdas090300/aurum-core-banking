package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.application.service.LoanProcessService;
import com.aurum.core_banking.interfaces.rest.dto.request.LoanApplicationRequest;
import com.aurum.core_banking.interfaces.rest.dto.request.LoanReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanProcessService loanProcessService;

    // Customer submits loan application → starts jBPM workflow
    @PostMapping("/applications")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('TELLER')")
    public ResponseEntity<?> applyForLoan(
            @RequestBody LoanApplicationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long processInstanceId = loanProcessService.submitLoanApplication(request);
        return ResponseEntity
            .created(URI.create("/api/v1/loans/applications/" + processInstanceId))
            .body(Map.of("processInstanceId", processInstanceId,
                         "status", "SUBMITTED"));
    }

    // Loan officer reviews a task in their queue
    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasRole('TELLER') or hasRole('MANAGER')")
    public ResponseEntity<?> completeReview(
            @PathVariable Long taskId,
            @RequestBody LoanReviewRequest review,
            @AuthenticationPrincipal Jwt jwt) {

        loanProcessService.completeLoanReview(
            taskId, jwt.getSubject(),
            review.approved(), review.notes());

        return ResponseEntity.ok(Map.of("taskId", taskId, "status", "COMPLETED"));
    }

    // Get pending tasks for logged-in officer
    @GetMapping("/tasks/queue")
    @PreAuthorize("hasRole('TELLER') or hasRole('MANAGER')")
    public ResponseEntity<?> getTaskQueue(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
            loanProcessService.getOfficerTaskQueue(jwt.getSubject()));
    }
}