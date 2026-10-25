package com.smartcare.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionMedicineDto {

    @NotBlank
    private String medicineName;

    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
}
