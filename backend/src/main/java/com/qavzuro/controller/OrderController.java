package com.qavzuro.controller;

import com.qavzuro.domain.Order;
import com.qavzuro.dto.request.UpdateOrderStatusRequest;
import com.qavzuro.dto.response.OrderResponse;
import com.qavzuro.mapper.OrderMapper;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponse>> myOrders(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderService.listForCustomer(currentUserService.getUserId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(orders.map(OrderMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable String id) {
        Order order = orderService.getById(id);
        boolean isStaff = currentUserService.getPrincipal().hasPermission("ORDER_VIEW");
        orderService.assertOwnedByOrStaff(order, currentUserService.getUserId(), isStaff);
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getByOrderNumber(@PathVariable String orderNumber) {
        Order order = orderService.getByOrderNumber(orderNumber);
        boolean isStaff = currentUserService.getPrincipal().hasPermission("ORDER_VIEW");
        orderService.assertOwnedByOrStaff(order, currentUserService.getUserId(), isStaff);
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable String id) {
        return ResponseEntity.ok(OrderMapper.toResponse(orderService.cancelByCustomer(currentUserService.getUserId(), id)));
    }

    // ---- Staff/admin ----

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    public ResponseEntity<Page<OrderResponse>> listAll(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        Page<Order> orders = orderService.listAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(orders.map(OrderMapper::toResponse));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable String id, @Valid @RequestBody UpdateOrderStatusRequest req) {
        Order order = orderService.transitionStatus(currentUserService.getUserId(), id, req.getStatus(),
                req.getNote(), req.getTrackingNumber(), req.getCarrier());
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }
}
