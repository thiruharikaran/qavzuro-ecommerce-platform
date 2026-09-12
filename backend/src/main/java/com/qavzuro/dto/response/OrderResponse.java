package com.qavzuro.dto.response;

import com.qavzuro.domain.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String orderNumber;
    private List<OrderItem> items;
    private double subtotal;
    private double discountTotal;
    private double taxTotal;
    private double shippingTotal;
    private double grandTotal;
    private Address shippingAddressSnapshot;
    private Address billingAddressSnapshot;
    private String shippingMethod;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String trackingNumber;
    private String carrier;
    private Instant estimatedDeliveryDate;
    private List<OrderStatusEvent> statusHistory;
    private Instant createdAt;
}
