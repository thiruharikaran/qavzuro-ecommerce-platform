package com.qavzuro.controller;

import com.qavzuro.domain.WorkforceTask;
import com.qavzuro.dto.request.CreateWorkforceTaskRequest;
import com.qavzuro.dto.request.UpdateWorkforceTaskStatusRequest;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.WorkforceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workforce")
@RequiredArgsConstructor
public class WorkforceController {

    private final WorkforceService workforceService;
    private final CurrentUserService currentUserService;

    @PostMapping("/tasks")
    @PreAuthorize("hasAuthority('WORKFORCE_TASK_ASSIGN')")
    public ResponseEntity<WorkforceTask> assign(@Valid @RequestBody CreateWorkforceTaskRequest req) {
        return ResponseEntity.ok(workforceService.assign(currentUserService.getUserId(), req));
    }

    @GetMapping("/tasks/assigned-to-me")
    public ResponseEntity<Page<WorkforceTask>> myTasks(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(workforceService.listAssignedTo(currentUserService.getUserId(), PageRequest.of(page, size)));
    }

    @GetMapping("/tasks/assigned-by-me")
    @PreAuthorize("hasAuthority('WORKFORCE_TASK_ASSIGN')")
    public ResponseEntity<Page<WorkforceTask>> tasksIAssigned(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(workforceService.listAssignedBy(currentUserService.getUserId(), PageRequest.of(page, size)));
    }

    @PatchMapping("/tasks/{id}/status")
    @PreAuthorize("hasAuthority('WORKFORCE_TASK_UPDATE_OWN')")
    public ResponseEntity<WorkforceTask> updateStatus(@PathVariable String id, @Valid @RequestBody UpdateWorkforceTaskStatusRequest req) {
        return ResponseEntity.ok(workforceService.updateOwnTaskStatus(currentUserService.getUserId(), id, req.getStatus()));
    }
}
