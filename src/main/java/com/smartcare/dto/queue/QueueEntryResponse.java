package com.smartcare.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One row in the doctor-facing live queue list. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntryResponse {
    private Long queueId;
    private Long appointmentId;
    private Integer tokenNumber;
    private String patientName;
    private String status;
    private LocalDateTime checkedInAt;
    private String consultationType;
}
