package com.qavzuro.controller;

import com.qavzuro.domain.InventoryHistoryEntry;
import com.qavzuro.dto.request.InventoryAdjustmentRequest;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;

    @PostMapping("/products/{productId}/adjust")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public ResponseEntity<Void> adjust(@PathVariable String productId, @Valid @RequestBody InventoryAdjustmentRequest req) {
        inventoryService.manualAdjust(currentUserService.getUserId(), productId, req.getVariantId(), req.getQuantityChange(), req.getReason());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{productId}/history")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<Page<InventoryHistoryEntry>> history(@PathVariable String productId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(inventoryService.history(productId, PageRequest.of(page, size)));
    }
}
