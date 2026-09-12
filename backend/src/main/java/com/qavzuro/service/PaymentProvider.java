package com.qavzuro.service;

/**
 * Abstraction over a payment provider so a real gateway (Stripe/Razorpay/etc.)
 * can be plugged in later without touching checkout logic. The only
 * implementation registered today is the sandbox/test provider.
 */
public interface PaymentProvider {
    PaymentResult charge(String orderReference, double amount, String currency, String paymentMethod);
    PaymentResult refund(String paymentId, double amount);
}
