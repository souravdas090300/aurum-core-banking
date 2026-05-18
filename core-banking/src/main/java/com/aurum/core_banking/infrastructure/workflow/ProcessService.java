package com.aurum.core_banking.infrastructure.workflow;

import java.util.Map;

/**
 * Process engine contract — abstracts jBPM / Kogito / stub implementations.
 *
 * Production implementation would delegate to a Kogito or jBPM 9.x runtime.
 * Current implementation: {@link InMemoryProcessService} (in-memory stub).
 */
public interface ProcessService {

    /**
     * Start a new process instance.
     *
     * @param deploymentId logical deployment identifier (e.g. "core-banking")
     * @param processId    process definition key (e.g. "loan-approval")
     * @param params       initial process variables
     * @return             the new process instance identifier
     */
    Long startProcess(String deploymentId, String processId, Map<String, Object> params);
}
