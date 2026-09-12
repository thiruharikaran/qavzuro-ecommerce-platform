package com.qavzuro.controller;

import com.qavzuro.dto.response.DashboardStatsResponse;
import com.qavzuro.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<DashboardStatsResponse> dashboard() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
