package com.qavzuro.service;

import com.qavzuro.domain.OrderStatus;

import java.util.Map;
import java.util.Set;

/** Defines which order-status transitions are legal. Prevents arbitrary jumps. */
public final class OrderStateMachine {
    private OrderStateMachine() {}

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.ofEntries(
            Map.entry(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PROCESSING, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.PACKED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED)),
            Map.entry(OrderStatus.SHIPPED, Set.of(OrderStatus.OUT_FOR_DELIVERY)),
            Map.entry(OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED)),
            Map.entry(OrderStatus.DELIVERED, Set.of(OrderStatus.RETURN_REQUESTED)),
            Map.entry(OrderStatus.RETURN_REQUESTED, Set.of(OrderStatus.RETURNED, OrderStatus.DELIVERED)),
            Map.entry(OrderStatus.RETURNED, Set.of(OrderStatus.REFUNDED)),
            Map.entry(OrderStatus.CANCELLED, Set.of()),
            Map.entry(OrderStatus.REFUNDED, Set.of())
    );

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == to) return false;
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<OrderStatus> allowedNext(OrderStatus from) {
        return TRANSITIONS.getOrDefault(from, Set.of());
    }
}
