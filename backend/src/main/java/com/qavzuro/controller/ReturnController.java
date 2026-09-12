package com.qavzuro.controller;

import com.qavzuro.domain.ReturnRequest;
import com.qavzuro.dto.request.CreateReturnRequest;
import com.qavzuro.dto.request.ReviewReturnRequest;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<ReturnRequest> create(@Valid @RequestBody CreateReturnRequest req) {
        return ResponseEntity.ok(returnService.create(currentUserService.getUserId(), req));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ReturnRequest>> myReturns(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(returnService.listForCustomer(currentUserService.getUserId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RETURN_VIEW')")
    public ResponseEntity<Page<ReturnRequest>> listAll(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(returnService.listAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAuthority('RETURN_APPROVE')")
    public ResponseEntity<ReturnRequest> review(@PathVariable String id, @Valid @RequestBody ReviewReturnRequest req) {
        return ResponseEntity.ok(returnService.review(currentUserService.getUserId(), id, req));
    }
}
