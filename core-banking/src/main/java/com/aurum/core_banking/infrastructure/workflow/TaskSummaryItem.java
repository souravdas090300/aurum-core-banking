package com.aurum.core_banking.infrastructure.workflow;

/**
 * Stub for org.kie.api.task.model.TaskSummary.
 * TODO Phase 4 — replace with the real jBPM TaskSummary model
 * once the process engine is integrated (see pom.xml TODO for jBPM 9.x).
 */
public interface TaskSummaryItem {

    Long getId();

    String getName();

    String getStatus();

    String getActualOwner();
}
