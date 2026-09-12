package com.qavzuro.service;

import com.qavzuro.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers valid/invalid order status transitions - the core of order state consistency. */
class OrderStateMachineTest {

    @Test
    void allowsNormalForwardProgression() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.PROCESSING, OrderStatus.PACKED));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.PACKED, OrderStatus.SHIPPED));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED));
    }

    @Test
    void rejectsSkippingSteps() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.SHIPPED),
                "Skipping from PENDING directly to SHIPPED must be rejected");
        assertFalse(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.DELIVERED));
    }

    @Test
    void rejectsTransitionsOutOfTerminalStates() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.CANCELLED, OrderStatus.CONFIRMED));
        assertFalse(OrderStateMachine.canTransition(OrderStatus.REFUNDED, OrderStatus.DELIVERED));
    }

    @Test
    void rejectsSelfTransition() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.PENDING));
    }

    @Test
    void allowsCancellationFromEarlyStates() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.CANCELLED));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
    }

    @Test
    void rejectsCancellationAfterShipped() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
                "A shipped order should not be directly cancellable");
    }

    @Test
    void allowsReturnFlow() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED));
        assertTrue(OrderStateMachine.canTransition(OrderStatus.RETURNED, OrderStatus.REFUNDED));
    }
}
