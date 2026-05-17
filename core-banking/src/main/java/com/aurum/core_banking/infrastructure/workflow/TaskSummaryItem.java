package com.aurum.core_banking.infrastructure.workflow;

/**
 * Minimal read projection of a human task — returned by the task queue API.
 */
public interface TaskSummaryItem {
    Long   getId();
    String getName();
    String getStatus();
    String getActualOwner();
}
