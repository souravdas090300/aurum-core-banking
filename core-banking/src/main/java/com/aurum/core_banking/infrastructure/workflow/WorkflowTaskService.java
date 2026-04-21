package com.aurum.core_banking.infrastructure.workflow;

import java.util.List;
import java.util.Map;

/**
 * Stub for the jBPM TaskService human-task API.
 * TODO Phase 4 — replace with the real jBPM / Kogito TaskService
 * once the process engine is integrated (see pom.xml TODO for jBPM 9.x).
 */
public interface WorkflowTaskService {

    void claim(Long taskId, String userId);

    void start(Long taskId, String userId);

    void complete(Long taskId, String userId, Map<String, Object> results);

    List<TaskSummaryItem> getTasksAssignedAsPotentialOwner(
            String userId, List<String> groupIds, String language, Object queryFilter);
}
