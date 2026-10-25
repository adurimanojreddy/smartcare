package com.smartcare.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Module 11 - live queue position card shown to a patient. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private Long appointmentId;
    private Integer currentToken;
    private Integer yourToken;
    private int patientsAhead;
    private int estimatedWaitingMinutes;
    private String status;
}
