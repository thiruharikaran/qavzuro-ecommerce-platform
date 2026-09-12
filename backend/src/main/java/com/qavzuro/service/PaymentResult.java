package com.qavzuro.service;

import com.qavzuro.domain.PaymentStatus;

public record PaymentResult(PaymentStatus status, String paymentId, String reference, String failureReason) {}
