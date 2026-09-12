package com.qavzuro.controller;

import com.qavzuro.domain.Order;
import com.qavzuro.dto.request.CheckoutRequest;
import com.qavzuro.dto.response.OrderResponse;
import com.qavzuro.mapper.OrderMapper;
import com.qavzuro.service.CheckoutService;
import com.qavzuro.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest req) {
        Order order = checkoutService.checkout(currentUserService.getUserId(), req);
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }
}
