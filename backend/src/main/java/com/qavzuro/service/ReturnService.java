package com.qavzuro.service;

import com.qavzuro.domain.*;
import com.qavzuro.dto.request.CreateReturnRequest;
import com.qavzuro.dto.request.ReviewReturnRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.OrderRepository;
import com.qavzuro.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentProvider paymentProvider;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public ReturnRequest create(String userId, CreateReturnRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        if (!order.getCustomerId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have access to this order.");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Only delivered orders are eligible for return.");
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .orderId(order.getId())
                .customerId(userId)
                .orderItemProductIds(req.getOrderItemProductIds() != null ? req.getOrderItemProductIds() : new ArrayList<>())
                .reason(req.getReason())
                .customerNote(req.getCustomerNote())
                .status(ReturnStatus.REQUESTED)
                .build();
        ReturnRequest saved = returnRequestRepository.save(returnRequest);

        order.setStatus(OrderStatus.RETURN_REQUESTED);
        order.getStatusHistory().add(OrderStatusEvent.builder()
                .status(OrderStatus.RETURN_REQUESTED).timestamp(java.time.Instant.now())
                .note("Return requested: " + req.getReason()).changedByUserId(userId).build());
        orderRepository.save(order);

        auditService.record(userId, null, "RETURN_REQUESTED", "RETURN", saved.getId(), null);
        return saved;
    }

    public Page<ReturnRequest> listForCustomer(String customerId, Pageable pageable) {
        return returnRequestRepository.findByCustomerId(customerId, pageable);
    }

    public Page<ReturnRequest> listAll(Pageable pageable) {
        return returnRequestRepository.findAll(pageable);
    }

    public ReturnRequest review(String staffUserId, String returnId, ReviewReturnRequest req) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found."));

        returnRequest.setStatus(req.getStatus());
        returnRequest.setReviewedByUserId(staffUserId);
        returnRequest.setReviewNote(req.getReviewNote());

        Order order = orderRepository.findById(returnRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        if (req.getStatus() == ReturnStatus.APPROVED) {
            notificationService.notify(returnRequest.getCustomerId(), NotificationType.RETURN_UPDATE,
                    "Return approved", "Your return request has been approved. Please ship the item(s) back.", returnRequest.getId());
        } else if (req.getStatus() == ReturnStatus.REJECTED) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
            notificationService.notify(returnRequest.getCustomerId(), NotificationType.RETURN_UPDATE,
                    "Return rejected", "Your return request was not approved: " + req.getReviewNote(), returnRequest.getId());
        } else if (req.getStatus() == ReturnStatus.ITEM_RECEIVED) {
            // Restock returned items.
            order.getItems().stream()
                    .filter(item -> returnRequest.getOrderItemProductIds().isEmpty()
                            || returnRequest.getOrderItemProductIds().contains(item.getProductId()))
                    .forEach(item -> inventoryService.manualAdjust(staffUserId, item.getProductId(), item.getVariantId(),
                            item.getQuantity(), "RETURN_RESTOCK"));
        } else if (req.getStatus() == ReturnStatus.REFUNDED) {
            double amount = req.getRefundAmount() != null ? req.getRefundAmount() : order.getGrandTotal();
            PaymentResult refund = paymentProvider.refund(order.getPaymentId(), amount);
            returnRequest.setRefundAmount(amount);
            returnRequest.setRefundReference(refund.reference());

            order.setStatus(OrderStatus.REFUNDED);
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.getStatusHistory().add(OrderStatusEvent.builder()
                    .status(OrderStatus.REFUNDED).timestamp(java.time.Instant.now())
                    .note("Refund processed").changedByUserId(staffUserId).build());
            orderRepository.save(order);

            notificationService.notify(returnRequest.getCustomerId(), NotificationType.REFUND,
                    "Refund processed", "A refund of " + amount + " has been processed for your return.", returnRequest.getId());
        }

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        auditService.record(staffUserId, null, "RETURN_" + req.getStatus(), "RETURN", returnId, null);
        return saved;
    }
}
