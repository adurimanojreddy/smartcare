package com.smartcare.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Module 10 - QR check-in confirmation screen. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private Long appointmentId;
    private Integer tokenNumber;
    private Integer currentToken;
    private int patientsAhead;
    private int estimatedWaitingMinutes;
}
