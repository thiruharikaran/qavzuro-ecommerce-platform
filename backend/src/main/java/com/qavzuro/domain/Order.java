package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private String customerId;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private double subtotal;
    private double discountTotal;
    private double taxTotal;
    private double shippingTotal;
    private double grandTotal;

    private String appliedCouponCode;

    private Address shippingAddressSnapshot;
    private Address billingAddressSnapshot;
    private String shippingMethod;

    @Indexed
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String paymentId;
    private String paymentReference;

    private String trackingNumber;
    private String carrier;
    private Instant estimatedDeliveryDate;

    @Builder.Default
    private List<OrderStatusEvent> statusHistory = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
