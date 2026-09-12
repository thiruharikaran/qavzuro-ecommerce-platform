package com.qavzuro.service;

import com.qavzuro.domain.Order;
import com.qavzuro.domain.OrderStatus;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.InvalidStateTransitionException;
import com.qavzuro.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Covers order ownership enforcement (IDOR protection) and state-machine-guarded transitions. */
class OrderServiceTest {

    private OrderRepository orderRepository;
    private InventoryService inventoryService;
    private NotificationService notificationService;
    private AuditService auditService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        inventoryService = mock(InventoryService.class);
        notificationService = mock(NotificationService.class);
        auditService = mock(AuditService.class);
        orderService = new OrderService(orderRepository, inventoryService, notificationService, auditService);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void assertOwnedByOrStaff_rejectsAccessByNonOwningNonStaffUser() {
        Order order = Order.builder().id("o1").customerId("owner-user").status(OrderStatus.PENDING).build();

        assertThrows(AccessDeniedException.class,
                () -> orderService.assertOwnedByOrStaff(order, "different-user", false),
                "A user who does not own the order and has no staff permission must be denied (IDOR protection)");
    }

    @Test
    void assertOwnedByOrStaff_allowsOwner() {
        Order order = Order.builder().id("o1").customerId("owner-user").status(OrderStatus.PENDING).build();
        assertDoesNotThrow(() -> orderService.assertOwnedByOrStaff(order, "owner-user", false));
    }

    @Test
    void assertOwnedByOrStaff_allowsStaffRegardlessOfOwnership() {
        Order order = Order.builder().id("o1").customerId("owner-user").status(OrderStatus.PENDING).build();
        assertDoesNotThrow(() -> orderService.assertOwnedByOrStaff(order, "staff-user", true));
    }

    @Test
    void transitionStatus_rejectsInvalidJump() {
        Order order = Order.builder().id("o1").customerId("u1").status(OrderStatus.PENDING).statusHistory(new ArrayList<>()).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThrows(InvalidStateTransitionException.class,
                () -> orderService.transitionStatus("staff-1", "o1", OrderStatus.SHIPPED, "skip ahead", null, null));
    }

    @Test
    void transitionStatus_toCancelled_releasesInventoryForEveryItem() {
        Order order = Order.builder().id("o1").customerId("u1").status(OrderStatus.CONFIRMED)
                .orderNumber("QVZ-1").statusHistory(new ArrayList<>())
                .items(java.util.List.of(
                        com.qavzuro.domain.OrderItem.builder().productId("p1").quantity(2).build(),
                        com.qavzuro.domain.OrderItem.builder().productId("p2").variantId("v1").quantity(1).build()
                )).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        orderService.transitionStatus("staff-1", "o1", OrderStatus.CANCELLED, "customer requested", null, null);

        verify(inventoryService).release(eq("p1"), isNull(), eq(2), eq("ORDER_CANCELLED"), eq("QVZ-1"), isNull());
        verify(inventoryService).release(eq("p2"), eq("v1"), eq(1), eq("ORDER_CANCELLED"), eq("QVZ-1"), isNull());
    }

    @Test
    void cancelByCustomer_rejectsCancellingAlreadyShippedOrder() {
        Order order = Order.builder().id("o1").customerId("u1").status(OrderStatus.SHIPPED).statusHistory(new ArrayList<>()).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelByCustomer("u1", "o1"));
    }

    @Test
    void cancelByCustomer_rejectsCancellingSomeoneElsesOrder() {
        Order order = Order.builder().id("o1").customerId("owner").status(OrderStatus.PENDING).statusHistory(new ArrayList<>()).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.cancelByCustomer("attacker", "o1"));
    }
}
