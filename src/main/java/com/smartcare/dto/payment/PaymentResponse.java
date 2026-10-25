package com.smartcare.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long appointmentId;
    private BigDecimal amount;
    private String status;
    private String method;
    private String transactionRef;
    private LocalDateTime paidAt;
}
