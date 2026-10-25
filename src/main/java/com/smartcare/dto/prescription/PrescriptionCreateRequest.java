package com.smartcare.dto.prescription;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Module 16 - doctor creates a digital prescription for an appointment. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionCreateRequest {

    @NotNull
    private Long appointmentId;

    private String diagnosisNotes;

    @NotEmpty(message = "At least one medicine is required")
    @Valid
    private List<PrescriptionMedicineDto> medicines;
}
