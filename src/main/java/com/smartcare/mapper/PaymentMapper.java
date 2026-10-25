package com.smartcare.mapper;

import com.smartcare.dto.payment.PaymentResponse;
import com.smartcare.entity.Payment;

public final class PaymentMapper {
    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment p) {
        if (p == null) return null;
        return PaymentResponse.builder()
                .id(p.getId())
                .appointmentId(p.getAppointment().getId())
                .amount(p.getAmount())
                .status(p.getStatus().name())
                .method(p.getMethod() != null ? p.getMethod().name() : null)
                .transactionRef(p.getTransactionRef())
                .paidAt(p.getPaidAt())
                .build();
    }
}
