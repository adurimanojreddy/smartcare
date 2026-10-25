package com.smartcare.mapper;

import com.smartcare.dto.prescription.PrescriptionMedicineDto;
import com.smartcare.dto.prescription.PrescriptionResponse;
import com.smartcare.entity.Prescription;
import com.smartcare.entity.PrescriptionMedicine;

import java.util.List;
import java.util.stream.Collectors;

public final class PrescriptionMapper {
    private PrescriptionMapper() {}

    public static PrescriptionResponse toResponse(Prescription p) {
        if (p == null) return null;
        List<PrescriptionMedicineDto> meds = p.getMedicines().stream()
                .map(PrescriptionMapper::toMedicineDto)
                .collect(Collectors.toList());
        return PrescriptionResponse.builder()
                .id(p.getId())
                .appointmentId(p.getAppointment().getId())
                .patientName(p.getPatient().getFullName())
                .doctorName(p.getDoctor().getFullName())
                .diagnosisNotes(p.getDiagnosisNotes())
                .medicines(meds)
                .createdAt(p.getCreatedAt())
                .build();
    }

    public static PrescriptionMedicineDto toMedicineDto(PrescriptionMedicine m) {
        return PrescriptionMedicineDto.builder()
                .medicineName(m.getMedicineName())
                .dosage(m.getDosage())
                .frequency(m.getFrequency())
                .duration(m.getDuration())
                .instructions(m.getInstructions())
                .build();
    }
}
