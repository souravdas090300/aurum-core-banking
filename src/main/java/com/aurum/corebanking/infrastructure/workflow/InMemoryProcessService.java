package com.aurum.corebanking.infrastructure.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of the process-engine contract.
 *
 * Stub — simulates jBPM/Kogito ProcessService behaviour without a full BPM runtime.
 * Stores active process instances in memory (not persisted across restarts).
 *
 * Replace with a real Kogito or jBPM 9.x integration in a production deployment.
 */
@Slf4j
@Service
public class InMemoryProcessService implements ProcessService {

    private final AtomicLong idSequence = new AtomicLong(1);
    private final ConcurrentHashMap<Long, ProcessInstance> instances = new ConcurrentHashMap<>();

    @Override
    public Long startProcess(String deploymentId, String processId, Map<String, Object> params) {
        long instanceId = idSequence.getAndIncrement();
        instances.put(instanceId, new ProcessInstance(instanceId, processId, params));
        log.info("Process started — deploymentId={} processId={} instanceId={} vars={}",
                 deploymentId, processId, instanceId, params.keySet());
        return instanceId;
    }

    /** Internal process instance record. */
    record ProcessInstance(long id, String processId, Map<String, Object> variables) {}
}
