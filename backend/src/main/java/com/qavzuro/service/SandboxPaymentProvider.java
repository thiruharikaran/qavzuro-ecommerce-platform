package com.qavzuro.service;

import com.qavzuro.domain.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Test/sandbox payment provider. No real payment credentials are required or
 * accepted. A paymentMethod value ending in "_DECLINE" simulates a failed
 * charge so failure-handling paths can be exercised without a live gateway.
 */
@Service
public class SandboxPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult charge(String orderReference, double amount, String currency, String paymentMethod) {
        String pm = paymentMethod == null ? "TEST_CARD" : paymentMethod;
        String paymentId = "pay_" + UUID.randomUUID();

        if (pm.toUpperCase().endsWith("_DECLINE")) {
            return new PaymentResult(PaymentStatus.FAILED, paymentId, null, "Test payment method configured to decline.");
        }
        String reference = "txn_" + UUID.randomUUID().toString().substring(0, 12);
        return new PaymentResult(PaymentStatus.PAID, paymentId, reference, null);
    }

    @Override
    public PaymentResult refund(String paymentId, double amount) {
        String reference = "rfnd_" + UUID.randomUUID().toString().substring(0, 12);
        return new PaymentResult(PaymentStatus.REFUNDED, paymentId, reference, null);
    }
}
