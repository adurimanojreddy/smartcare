package com.smartcare.mapper;

import com.smartcare.dto.patient.PatientResponse;
import com.smartcare.entity.Patient;

public final class PatientMapper {
    private PatientMapper() {}

    public static PatientResponse toResponse(Patient p) {
        if (p == null) return null;
        return PatientResponse.builder()
                .id(p.getId())
                .fullName(p.getFullName())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender() != null ? p.getGender().name() : null)
                .phone(p.getPhone())
                .email(p.getEmail())
                .address(p.getAddress())
                .emergencyContact(p.getEmergencyContact())
                .bloodGroup(p.getBloodGroup())
                .profilePhotoUrl(p.getProfilePhotoUrl())
                .registrationDate(p.getRegistrationDate())
                .build();
    }
}
