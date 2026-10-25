package com.smartcare.service.impl;

import com.smartcare.dto.payment.PaymentRequest;
import com.smartcare.dto.payment.PaymentResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.Payment;
import com.smartcare.entity.enums.PaymentMethod;
import com.smartcare.entity.enums.PaymentStatus;
import com.smartcare.exception.AppointmentNotFoundException;
import com.smartcare.exception.PaymentException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.mapper.PaymentMapper;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.PaymentRepository;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Module 9 - PaymentService abstraction (createPayment / processPayment /
 * refundPayment). This project ships a mock gateway: processPayment always
 * succeeds and issues a fake transaction reference. Swapping in a real
 * gateway (Razorpay/Stripe/etc.) only requires changing processPayment's
 * body - the rest of the appointment flow is gateway-agnostic.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

        if (appointment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentException("This appointment has already been paid for");
        }

        BigDecimal amount = appointment.getDoctor().getConsultationFee() != null
                ? appointment.getDoctor().getConsultationFee() : BigDecimal.ZERO;

        Payment payment = paymentRepository.findByAppointmentId(appointment.getId())
                .orElseGet(() -> Payment.builder()
                        .appointment(appointment)
                        .amount(amount)
                        .status(PaymentStatus.PENDING)
                        .build());

        payment.setMethod(parseMethod(request.getMethod()));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(amount);

        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        Payment payment = findOrThrow(paymentId);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("Only pending payments can be processed (current status: "
                    + payment.getStatus() + ")");
        }

        // ---- Mock gateway (Module 9/29: this is a demo, not a real gateway) ----
        boolean gatewaySuccess = true;
        String transactionRef = "MOCK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        // -------------------------------------------------------------------

        if (gatewaySuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionRef(transactionRef);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Mock gateway declined the transaction");
        }
        payment = paymentRepository.save(payment);

        appointmentService.applyPaymentResult(payment.getAppointment().getId(), gatewaySuccess);

        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = findOrThrow(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentException("Only successful payments can be refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        Appointment appointment = payment.getAppointment();
        appointment.setPaymentStatus(PaymentStatus.REFUNDED);
        appointmentRepository.save(appointment);

        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByAppointment(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .map(PaymentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment record found for appointment id: " + appointmentId));
    }

    private Payment findOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }

    private PaymentMethod parseMethod(String raw) {
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new PaymentException("Invalid payment method: " + raw);
        }
    }
}
