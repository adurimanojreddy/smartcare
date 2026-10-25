package com.smartcare.controller;

import com.smartcare.dto.payment.PaymentRequest;
import com.smartcare.dto.payment.PaymentResponse;
import com.smartcare.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Module 9/20 - Online Payment (mock gateway for this project - see Module 29). */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PaymentResponse> process(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.processPayment(paymentId));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<PaymentResponse> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.getByAppointment(appointmentId));
    }
}
