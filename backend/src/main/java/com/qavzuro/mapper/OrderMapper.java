package com.qavzuro.mapper;

import com.qavzuro.domain.Order;
import com.qavzuro.dto.response.OrderResponse;

public final class OrderMapper {
    private OrderMapper() {}

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .items(order.getItems())
                .subtotal(order.getSubtotal())
                .discountTotal(order.getDiscountTotal())
                .taxTotal(order.getTaxTotal())
                .shippingTotal(order.getShippingTotal())
                .grandTotal(order.getGrandTotal())
                .shippingAddressSnapshot(order.getShippingAddressSnapshot())
                .billingAddressSnapshot(order.getBillingAddressSnapshot())
                .shippingMethod(order.getShippingMethod())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .statusHistory(order.getStatusHistory())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
