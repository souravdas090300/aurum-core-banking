package com.aurum.core_banking.infrastructure.workflow;

import java.util.Map;

/**
 * Stub for org.jbpm.services.api.ProcessService.
 * TODO Phase 4 — replace with real jBPM / Kogito ProcessService once
 * the process engine is integrated (see pom.xml TODO for jBPM 9.x).
 */
public interface ProcessService {

    Long startProcess(String deploymentId, String processId, Map<String, Object> params);
}
