package com.qavzuro.service;

import com.qavzuro.domain.*;
import com.qavzuro.dto.request.CreateReturnRequest;
import com.qavzuro.dto.request.ReviewReturnRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.repository.OrderRepository;
import com.qavzuro.repository.ReturnRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Covers return eligibility rules, ownership checks, and the refund path. */
class ReturnServiceTest {

    private ReturnRequestRepository returnRequestRepository;
    private OrderRepository orderRepository;
    private InventoryService inventoryService;
    private PaymentProvider paymentProvider;
    private NotificationService notificationService;
    private AuditService auditService;
    private ReturnService returnService;

    @BeforeEach
    void setUp() {
        returnRequestRepository = mock(ReturnRequestRepository.class);
        orderRepository = mock(OrderRepository.class);
        inventoryService = mock(InventoryService.class);
        paymentProvider = mock(PaymentProvider.class);
        notificationService = mock(NotificationService.class);
        auditService = mock(AuditService.class);
        returnService = new ReturnService(returnRequestRepository, orderRepository, inventoryService,
                paymentProvider, notificationService, auditService);

        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(inv -> {
            ReturnRequest r = inv.getArgument(0);
            if (r.getId() == null) r.setId("ret-1");
            return r;
        });
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_rejectsReturnForNonDeliveredOrder() {
        Order order = Order.builder().id("o1").customerId("u1").status(OrderStatus.SHIPPED).statusHistory(new ArrayList<>()).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        CreateReturnRequest req = new CreateReturnRequest();
        req.setOrderId("o1"); req.setReason("Changed my mind");

        assertThrows(BadRequestException.class, () -> returnService.create("u1", req));
    }

    @Test
    void create_rejectsReturnRequestForSomeoneElsesOrder() {
        Order order = Order.builder().id("o1").customerId("owner").status(OrderStatus.DELIVERED).statusHistory(new ArrayList<>()).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        CreateReturnRequest req = new CreateReturnRequest();
        req.setOrderId("o1"); req.setReason("Not mine to return");

        assertThrows(AccessDeniedException.class, () -> returnService.create("attacker", req));
    }

    @Test
    void create_succeedsForDeliveredOwnedOrder_andMovesOrderToReturnRequested() {
        Order order = Order.builder().id("o1").customerId("u1").status(OrderStatus.DELIVERED).statusHistory(new ArrayList<>()).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        CreateReturnRequest req = new CreateReturnRequest();
        req.setOrderId("o1"); req.setReason("Defective item");

        ReturnRequest saved = returnService.create("u1", req);

        assertEquals(ReturnStatus.REQUESTED, saved.getStatus());
        assertEquals(OrderStatus.RETURN_REQUESTED, order.getStatus());
    }

    @Test
    void review_refunded_callsPaymentProviderAndUpdatesOrder() {
        ReturnRequest returnRequest = ReturnRequest.builder().id("ret-1").orderId("o1").customerId("u1")
                .orderItemProductIds(new ArrayList<>()).status(ReturnStatus.APPROVED).build();
        Order order = Order.builder().id("o1").customerId("u1").paymentId("pay_1").grandTotal(500.0)
                .status(OrderStatus.RETURN_REQUESTED).statusHistory(new ArrayList<>()).items(new ArrayList<>()).build();

        when(returnRequestRepository.findById("ret-1")).thenReturn(Optional.of(returnRequest));
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(paymentProvider.refund(eq("pay_1"), eq(500.0)))
                .thenReturn(new PaymentResult(PaymentStatus.REFUNDED, "pay_1", "rfnd_123", null));

        ReviewReturnRequest req = new ReviewReturnRequest();
        req.setStatus(ReturnStatus.REFUNDED);

        ReturnRequest result = returnService.review("staff-1", "ret-1", req);

        assertEquals(500.0, result.getRefundAmount());
        assertEquals("rfnd_123", result.getRefundReference());
        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        assertEquals(PaymentStatus.REFUNDED, order.getPaymentStatus());
        verify(paymentProvider).refund("pay_1", 500.0);
    }

    @Test
    void review_rejected_revertsOrderBackToDelivered() {
        ReturnRequest returnRequest = ReturnRequest.builder().id("ret-1").orderId("o1").customerId("u1")
                .orderItemProductIds(new ArrayList<>()).status(ReturnStatus.REQUESTED).build();
        Order order = Order.builder().id("o1").customerId("u1").status(OrderStatus.RETURN_REQUESTED)
                .statusHistory(new ArrayList<>()).items(new ArrayList<>()).build();

        when(returnRequestRepository.findById("ret-1")).thenReturn(Optional.of(returnRequest));
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        ReviewReturnRequest req = new ReviewReturnRequest();
        req.setStatus(ReturnStatus.REJECTED);
        req.setReviewNote("Outside return window");

        returnService.review("staff-1", "ret-1", req);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void review_itemReceived_restocksEachReturnedItem() {
        ReturnRequest returnRequest = ReturnRequest.builder().id("ret-1").orderId("o1").customerId("u1")
                .orderItemProductIds(new ArrayList<>()).status(ReturnStatus.APPROVED).build();
        Order order = Order.builder().id("o1").customerId("u1").status(OrderStatus.RETURN_REQUESTED)
                .statusHistory(new ArrayList<>())
                .items(java.util.List.of(OrderItem.builder().productId("p1").quantity(2).build()))
                .build();

        when(returnRequestRepository.findById("ret-1")).thenReturn(Optional.of(returnRequest));
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        ReviewReturnRequest req = new ReviewReturnRequest();
        req.setStatus(ReturnStatus.ITEM_RECEIVED);

        returnService.review("staff-1", "ret-1", req);

        verify(inventoryService).manualAdjust("staff-1", "p1", null, 2, "RETURN_RESTOCK");
    }
}
