package com.qavzuro.service;

import com.qavzuro.domain.NotificationType;
import com.qavzuro.domain.Order;
import com.qavzuro.domain.OrderStatus;
import com.qavzuro.domain.OrderStatusEvent;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.InvalidStateTransitionException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public Order getById(String id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }

    public Order getByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }

    public void assertOwnedByOrStaff(Order order, String userId, boolean isStaff) {
        if (!isStaff && !order.getCustomerId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have access to this order.");
        }
    }

    public Page<Order> listForCustomer(String customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Order> listAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public String generateOrderNumber() {
        String candidate;
        do {
            candidate = "QVZ-" + System.currentTimeMillis() + "-" + (100 + RANDOM.nextInt(900));
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }

    public Order transitionStatus(String actorUserId, String orderId, OrderStatus newStatus, String note,
                                   String trackingNumber, String carrier) {
        Order order = getById(orderId);
        if (!com.qavzuro.service.OrderStateMachine.canTransition(order.getStatus(), newStatus)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition order from " + order.getStatus() + " to " + newStatus + ".");
        }
        order.setStatus(newStatus);
        if (trackingNumber != null) order.setTrackingNumber(trackingNumber);
        if (carrier != null) order.setCarrier(carrier);
        order.getStatusHistory().add(OrderStatusEvent.builder()
                .status(newStatus).timestamp(Instant.now()).note(note).changedByUserId(actorUserId).build());

        if (newStatus == OrderStatus.CANCELLED) {
            releaseInventoryForOrder(order, "ORDER_CANCELLED");
        }

        Order saved = orderRepository.save(order);
        auditService.record(actorUserId, null, "ORDER_STATUS_CHANGED", "ORDER", order.getId(),
                java.util.Map.of("newStatus", newStatus.name()));
        notificationService.notify(order.getCustomerId(), NotificationType.SHIPMENT_UPDATE,
                "Order " + order.getOrderNumber() + " update", "Your order status is now " + newStatus + ".", order.getId());
        return saved;
    }

    public Order cancelByCustomer(String userId, String orderId) {
        Order order = getById(orderId);
        assertOwnedByOrStaff(order, userId, false);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("This order can no longer be cancelled. Please request a return instead.");
        }
        return transitionStatus(userId, orderId, OrderStatus.CANCELLED, "Cancelled by customer", null, null);
    }

    private void releaseInventoryForOrder(Order order, String reason) {
        order.getItems().forEach(item ->
                inventoryService.release(item.getProductId(), item.getVariantId(), item.getQuantity(), reason, order.getOrderNumber(), null));
    }
}
