package com.smartcare.dto.prescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long appointmentId;
    private String patientName;
    private String doctorName;
    private String diagnosisNotes;
    private List<PrescriptionMedicineDto> medicines;
    private LocalDateTime createdAt;
}
