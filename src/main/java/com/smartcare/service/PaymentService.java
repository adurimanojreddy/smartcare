package com.smartcare.service;

import com.smartcare.dto.payment.PaymentRequest;
import com.smartcare.dto.payment.PaymentResponse;

/**
 * Module 9 - Online Payment. No card numbers or sensitive payment data are
 * ever accepted or stored; only a mock/real gateway transaction reference.
 */
public interface PaymentService {

    /** Creates a PENDING payment record for an appointment. */
    PaymentResponse createPayment(PaymentRequest request);

    /** Processes a pending payment. Uses a mock gateway for this project (Module 9/29). */
    PaymentResponse processPayment(Long paymentId);

    PaymentResponse refundPayment(Long paymentId);

    PaymentResponse getByAppointment(Long appointmentId);
}
