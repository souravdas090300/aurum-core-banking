package com.aurum.corebanking.infrastructure.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the human-task service contract.
 *
 * Stub — simulates jBPM/Kogito TaskService behaviour without a full BPM runtime.
 * Tasks are stored in memory (not persisted across restarts).
 *
 * Replace with a real Kogito or jBPM 9.x integration in a production deployment.
 */
@Slf4j
@Service
public class InMemoryWorkflowTaskService implements WorkflowTaskService {

    private final ConcurrentHashMap<Long, TaskRecord> tasks = new ConcurrentHashMap<>();

    /** Register a new task (called internally when a process reaches a UserTask node). */
    public void registerTask(Long taskId, String name, String groupId) {
        tasks.put(taskId, new TaskRecord(taskId, name, "Ready", null, groupId));
        log.info("Task registered — taskId={} name={} group={}", taskId, name, groupId);
    }

    @Override
    public void claim(Long taskId, String userId) {
        tasks.computeIfPresent(taskId, (id, t) -> t.withOwnerAndStatus(userId, "Reserved"));
        log.info("Task claimed — taskId={} userId={}", taskId, userId);
    }

    @Override
    public void start(Long taskId, String userId) {
        tasks.computeIfPresent(taskId, (id, t) -> t.withOwnerAndStatus(userId, "InProgress"));
        log.info("Task started — taskId={} userId={}", taskId, userId);
    }

    @Override
    public void complete(Long taskId, String userId, Map<String, Object> results) {
        tasks.computeIfPresent(taskId, (id, t) -> t.withOwnerAndStatus(userId, "Completed"));
        log.info("Task completed — taskId={} userId={} results={}", taskId, userId, results.keySet());
    }

    @Override
    public List<TaskSummaryItem> getTasksAssignedAsPotentialOwner(
            String userId, List<String> groupIds, String language, Object queryFilter) {
        return tasks.values().stream()
            .filter(t -> ("Ready".equals(t.status()) || "Reserved".equals(t.status()))
                      && groupIds != null && groupIds.contains(t.groupId()))
            .map(t -> (TaskSummaryItem) t)
            .collect(Collectors.toList());
    }

    /** Internal task record — also implements TaskSummaryItem for the queue API. */
    record TaskRecord(Long id, String name, String status, String owner, String groupId)
            implements TaskSummaryItem {

        TaskRecord withOwnerAndStatus(String newOwner, String newStatus) {
            return new TaskRecord(id, name, newStatus, newOwner, groupId);
        }

        @Override public Long getId()          { return id; }
        @Override public String getName()       { return name; }
        @Override public String getStatus()     { return status; }
        @Override public String getActualOwner() { return owner; }
    }
}
