package com.smartcare.dto.medicalrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Module 15 - one entry in the patient's digital medical timeline. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalTimelineEntryResponse {
    private Long id;
    private String recordType; // CONSULTATION / DIAGNOSIS / PRESCRIPTION / LAB_REQUEST / LAB_REPORT / APPOINTMENT
    private String description;
    private LocalDate recordDate;
    private String doctorName;
    private Long appointmentId;
}
