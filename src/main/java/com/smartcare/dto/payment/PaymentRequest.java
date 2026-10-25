package com.smartcare.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Module 9 - initiate payment for a booked appointment. No card data ever accepted here. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull
    private Long appointmentId;

    @NotNull
    private String method; // CARD / UPI / NET_BANKING / WALLET / CASH_AT_HOSPITAL / MOCK_GATEWAY
}
