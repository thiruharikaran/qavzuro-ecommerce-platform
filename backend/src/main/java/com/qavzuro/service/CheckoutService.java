package com.qavzuro.service;

import com.qavzuro.domain.*;
import com.qavzuro.dto.request.CheckoutRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.InsufficientStockException;
import com.qavzuro.repository.CartRepository;
import com.qavzuro.repository.OrderRepository;
import com.qavzuro.repository.ProductRepository;
import com.qavzuro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The checkout flow is the one place where money actually moves, so every
 * number here is recomputed from live server data. Nothing supplied by the
 * client (price, discount, tax, shipping, total) is ever trusted.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final PaymentProvider paymentProvider;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    private static final double STANDARD_SHIPPING = 49.0;
    private static final double EXPRESS_SHIPPING = 149.0;
    private static final double FREE_SHIPPING_THRESHOLD = 999.0;

    public Order checkout(String userId, CheckoutRequest req) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Your cart is empty."));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found."));
        Address shippingAddress = user.getAddresses().stream()
                .filter(a -> a.getId().equals(req.getShippingAddressId())).findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid shipping address."));
        Address billingAddress = user.getAddresses().stream()
                .filter(a -> a.getId().equals(req.getBillingAddressId())).findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid billing address."));

        // --- Step 1: recompute every line item authoritatively from live product data ---
        List<OrderItem> orderItems = new ArrayList<>();
        List<String> reserved = new ArrayList<>(); // productId|variantId already reserved, for rollback on failure
        double subtotal = 0;
        double taxTotal = 0;

        try {
            for (CartItem cartItem : cart.getItems()) {
                Product product = productRepository.findById(cartItem.getProductId())
                        .orElseThrow(() -> new BadRequestException("A product in your cart is no longer available."));
                if (product.getStatus() != ProductStatus.ACTIVE) {
                    throw new BadRequestException(product.getName() + " is no longer available.");
                }

                double unitPrice = product.getEffectivePrice();
                if (cartItem.getVariantId() != null) {
                    ProductVariant variant = product.getVariants().stream()
                            .filter(v -> v.getVariantId().equals(cartItem.getVariantId())).findFirst()
                            .orElseThrow(() -> new BadRequestException("A selected product variant is no longer available."));
                    if (variant.getPriceOverride() != null) unitPrice = variant.getPriceOverride();
                }

                // Atomic reservation - throws InsufficientStockException if not enough stock.
                inventoryService.reserve(product.getId(), cartItem.getVariantId(), cartItem.getQuantity(),
                        "ORDER_PLACED", null, userId);
                reserved.add(product.getId() + "|" + (cartItem.getVariantId() == null ? "" : cartItem.getVariantId()) + "|" + cartItem.getQuantity());

                double lineSubtotal = unitPrice * cartItem.getQuantity();
                double lineTax = lineSubtotal * (product.getTaxRatePercent() / 100.0);

                orderItems.add(OrderItem.builder()
                        .productId(product.getId())
                        .variantId(cartItem.getVariantId())
                        .sku(product.getSku())
                        .productNameSnapshot(product.getName())
                        .imageUrlSnapshot(product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl())
                        .quantity(cartItem.getQuantity())
                        .unitPriceSnapshot(round(unitPrice))
                        .lineDiscount(0) // filled in after coupon discount is allocated below
                        .lineTax(round(lineTax))
                        .lineTotal(round(lineSubtotal + lineTax))
                        .build());

                subtotal += lineSubtotal;
                taxTotal += lineTax;
            }

            // --- Step 2: coupon discount, validated and allocated proportionally per line ---
            double discountTotal = 0;
            String appliedCoupon = req.getCouponCode() != null ? req.getCouponCode() : cart.getAppliedCouponCode();
            if (appliedCoupon != null && !appliedCoupon.isBlank()) {
                discountTotal = couponService.validateAndCalculateDiscount(appliedCoupon, userId, subtotal);
                if (subtotal > 0) {
                    double finalSubtotal = subtotal;
                    double finalDiscountTotal = discountTotal;
                    for (int i = 0; i < orderItems.size(); i++) {
                        OrderItem item = orderItems.get(i);
                        double lineSubtotal = item.getUnitPriceSnapshot() * item.getQuantity();
                        double lineDiscount = finalDiscountTotal * (lineSubtotal / finalSubtotal);
                        item.setLineDiscount(round(lineDiscount));
                        item.setLineTotal(round(lineSubtotal + item.getLineTax() - lineDiscount));
                    }
                }
            }

            // --- Step 3: shipping ---
            double shipping = "EXPRESS".equalsIgnoreCase(req.getShippingMethod()) ? EXPRESS_SHIPPING : STANDARD_SHIPPING;
            if (subtotal - discountTotal >= FREE_SHIPPING_THRESHOLD && !"EXPRESS".equalsIgnoreCase(req.getShippingMethod())) {
                shipping = 0;
            }

            double grandTotal = round(subtotal - discountTotal + taxTotal + shipping);

            // --- Step 4: create the order in PENDING state, then attempt payment ---
            Order order = Order.builder()
                    .orderNumber(orderService.generateOrderNumber())
                    .customerId(userId)
                    .items(orderItems)
                    .subtotal(round(subtotal))
                    .discountTotal(round(discountTotal))
                    .taxTotal(round(taxTotal))
                    .shippingTotal(round(shipping))
                    .grandTotal(grandTotal)
                    .appliedCouponCode(appliedCoupon)
                    .shippingAddressSnapshot(shippingAddress)
                    .billingAddressSnapshot(billingAddress)
                    .shippingMethod(req.getShippingMethod())
                    .status(OrderStatus.PENDING)
                    .paymentStatus(PaymentStatus.PENDING)
                    .statusHistory(new ArrayList<>(List.of(OrderStatusEvent.builder()
                            .status(OrderStatus.PENDING).timestamp(Instant.now()).note("Order created").changedByUserId(userId).build())))
                    .build();
            order = orderRepository.save(order);

            PaymentResult payment = paymentProvider.charge(order.getOrderNumber(), grandTotal, "INR", req.getPaymentMethod());

            if (payment.status() == PaymentStatus.PAID) {
                // Commit reservations into real stock deductions now that payment succeeded.
                for (OrderItem item : orderItems) {
                    inventoryService.commitReservation(item.getProductId(), item.getVariantId(), item.getQuantity(),
                            "ORDER_PLACED", order.getOrderNumber(), userId);
                }
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setPaymentId(payment.paymentId());
                order.setPaymentReference(payment.reference());
                order.setStatus(OrderStatus.CONFIRMED);
                order.getStatusHistory().add(OrderStatusEvent.builder()
                        .status(OrderStatus.CONFIRMED).timestamp(Instant.now()).note("Payment received").changedByUserId(userId).build());

                if (appliedCoupon != null && !appliedCoupon.isBlank()) {
                    couponService.incrementUsage(appliedCoupon);
                }

                cart.getItems().clear();
                cart.setAppliedCouponCode(null);
                cartRepository.save(cart);

                order = orderRepository.save(order);

                auditService.record(userId, user.getEmail(), "ORDER_PLACED", "ORDER", order.getId(),
                        java.util.Map.of("orderNumber", order.getOrderNumber(), "grandTotal", grandTotal));
                notificationService.notify(userId, NotificationType.ORDER_CONFIRMATION,
                        "Order confirmed", "Your order " + order.getOrderNumber() + " has been confirmed.", order.getId());
                notificationService.notify(userId, NotificationType.PAYMENT_RESULT,
                        "Payment successful", "Payment for order " + order.getOrderNumber() + " was successful.", order.getId());

                return order;
            } else {
                // Payment failed: release every reservation, mark the order cancelled, and surface the failure.
                for (OrderItem item : orderItems) {
                    inventoryService.release(item.getProductId(), item.getVariantId(), item.getQuantity(),
                            "PAYMENT_FAILED", order.getOrderNumber(), userId);
                }
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setPaymentId(payment.paymentId());
                order.setStatus(OrderStatus.CANCELLED);
                order.getStatusHistory().add(OrderStatusEvent.builder()
                        .status(OrderStatus.CANCELLED).timestamp(Instant.now())
                        .note("Payment failed: " + payment.failureReason()).changedByUserId(userId).build());
                orderRepository.save(order);

                notificationService.notify(userId, NotificationType.PAYMENT_RESULT,
                        "Payment failed", "Payment for order " + order.getOrderNumber() + " failed: " + payment.failureReason(), order.getId());

                throw new BadRequestException("Payment failed: " + payment.failureReason());
            }

        } catch (InsufficientStockException | BadRequestException ex) {
            // Roll back any reservations made before the failure.
            for (String r : reserved) {
                String[] parts = r.split("\\|", -1);
                String pid = parts[0];
                String vid = parts[1].isEmpty() ? null : parts[1];
                int qty = Integer.parseInt(parts[2]);
                try {
                    inventoryService.release(pid, vid, qty, "CHECKOUT_ROLLBACK", null, userId);
                } catch (Exception ignored) { /* best-effort rollback */ }
            }
            throw ex;
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
