package com.aurum.corebanking.infrastructure.workflow;

import java.util.List;
import java.util.Map;

/**
 * Human-task service contract — abstracts jBPM / Kogito / stub implementations.
 *
 * Current implementation: {@link InMemoryWorkflowTaskService} (in-memory stub).
 */
public interface WorkflowTaskService {

    /** Claim a task for a specific user (moves status from Ready → Reserved). */
    void claim(Long taskId, String userId);

    /** Start working on a claimed task (moves status from Reserved → InProgress). */
    void start(Long taskId, String userId);

    /** Complete a task with output variables. */
    void complete(Long taskId, String userId, Map<String, Object> results);

    /**
     * Return tasks eligible for the given user or any of their groups.
     *
     * @param userId      the actor user ID
     * @param groupIds    group memberships (roles)
     * @param language    locale hint (e.g. "en-UK")
     * @param queryFilter optional filter object (implementation-specific)
     */
    List<TaskSummaryItem> getTasksAssignedAsPotentialOwner(
            String userId, List<String> groupIds, String language, Object queryFilter);
}
