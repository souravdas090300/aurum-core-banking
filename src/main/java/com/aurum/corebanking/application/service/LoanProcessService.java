package com.aurum.corebanking.application.service;

import com.aurum.corebanking.domain.rules.CustomerFact;
import com.aurum.corebanking.infrastructure.workflow.ProcessService;
import com.aurum.corebanking.infrastructure.workflow.WorkflowTaskService;
import com.aurum.corebanking.infrastructure.workflow.TaskSummaryItem;
import com.aurum.corebanking.interfaces.rest.dto.request.LoanApplicationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProcessService {

    private final ProcessService        processService;
    private final WorkflowTaskService   taskService;
    private final CreditScoringService  creditScoringService;

    private static final String DEPLOYMENT_ID = "core-banking";
    private static final String PROCESS_ID    = "loan-approval";

    /**
     * Start a new loan approval workflow instance.
     * Returns the jBPM process instance ID for tracking.
     */
    public Long submitLoanApplication(LoanApplicationRequest request) {

        // 1. Run credit scoring via Drools
        CustomerFact customerFact = buildCustomerFact(request);
        String decision = creditScoringService.evaluate(customerFact);

        // 2. Prepare process variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerId",      request.customerId().toString());
        variables.put("requestedAmount", request.amount());
        variables.put("termMonths",      request.termMonths());
        variables.put("loanDecision",    decision);        // feeds BPMN gateway
        variables.put("creditScore",     customerFact.getCreditScore());
        variables.put("riskScore",       customerFact.getRiskScore());
        variables.put("applicantName",   customerFact.getFullName());

        // 3. Start process via workflow engine
        Long processInstanceId = processService.startProcess(
            DEPLOYMENT_ID,
            PROCESS_ID,
            variables
        );

        log.info("Loan process started — processId={} customerId={} decision={}",
            processInstanceId, request.customerId(), decision);

        return processInstanceId;
    }

    /**
     * Loan officer claims and completes a manual review task.
     */
    public void completeLoanReview(Long taskId, String officerUserId,
                                   boolean approved, String notes) {
        // Claim the task
        taskService.claim(taskId, officerUserId);
        taskService.start(taskId, officerUserId);

        // Set outcome variables
        Map<String, Object> results = new HashMap<>();
        results.put("approved",    approved);
        results.put("reviewNotes", notes);
        results.put("reviewedBy",  officerUserId);
        results.put("reviewedAt",  new Date());

        taskService.complete(taskId, officerUserId, results);

        log.info("Loan review completed — taskId={} officer={} approved={}",
            taskId, officerUserId, approved);
    }

    /**
     * Get pending tasks for a loan officer's queue.
     */
    public List<TaskSummaryItem> getOfficerTaskQueue(String officerUserId) {
        return taskService.getTasksAssignedAsPotentialOwner(
            officerUserId,
            Collections.singletonList("loan-managers"),
            "en-UK",
            null
        );
    }

    private CustomerFact buildCustomerFact(LoanApplicationRequest req) {
        return CustomerFact.builder()
            .customerId(req.customerId().toString())
            .fullName(req.fullName())
            .creditScore(req.creditScore())
            .debtToIncomeRatio(req.debtToIncomeRatio())
            .monthlyIncome(req.monthlyIncome())
            .requestedAmount(req.amount().doubleValue())
            .termMonths(req.termMonths())
            .pep(req.isPep())
            .build();
    }
}