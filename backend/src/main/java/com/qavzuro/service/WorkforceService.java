package com.qavzuro.service;

import com.qavzuro.domain.NotificationType;
import com.qavzuro.domain.WorkforceTask;
import com.qavzuro.dto.request.CreateWorkforceTaskRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.WorkforceTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkforceService {

    private static final Set<String> VALID_STATUSES = Set.of("OPEN", "IN_PROGRESS", "DONE", "CANCELLED");

    private final WorkforceTaskRepository workforceTaskRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public WorkforceTask assign(String assignedByUserId, CreateWorkforceTaskRequest req) {
        WorkforceTask task = WorkforceTask.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .taskType(req.getTaskType())
                .referenceOrderId(req.getReferenceOrderId())
                .assignedToUserId(req.getAssignedToUserId())
                .assignedByUserId(assignedByUserId)
                .status("OPEN")
                .dueAt(req.getDueAt())
                .build();
        WorkforceTask saved = workforceTaskRepository.save(task);

        notificationService.notify(req.getAssignedToUserId(), NotificationType.WORKFORCE_EVENT,
                "New task assigned", "You have been assigned: " + req.getTitle(), saved.getId());
        auditService.record(assignedByUserId, null, "WORKFORCE_TASK_ASSIGNED", "WORKFORCE_TASK", saved.getId(), null);
        return saved;
    }

    public Page<WorkforceTask> listAssignedTo(String userId, Pageable pageable) {
        return workforceTaskRepository.findByAssignedToUserId(userId, pageable);
    }

    public Page<WorkforceTask> listAssignedBy(String userId, Pageable pageable) {
        return workforceTaskRepository.findByAssignedByUserId(userId, pageable);
    }

    /** A worker may only update the status of tasks assigned to them. */
    public WorkforceTask updateOwnTaskStatus(String userId, String taskId, String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BadRequestException("Invalid task status.");
        }
        WorkforceTask task = workforceTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
        if (!task.getAssignedToUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only update your own assigned tasks.");
        }
        task.setStatus(status);
        if ("DONE".equals(status)) {
            task.setCompletedAt(java.time.Instant.now());
        }
        return workforceTaskRepository.save(task);
    }
}
