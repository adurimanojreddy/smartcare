package com.smartcare.mapper;

import com.smartcare.dto.medicalrecord.MedicalTimelineEntryResponse;
import com.smartcare.entity.MedicalRecord;

public final class MedicalRecordMapper {
    private MedicalRecordMapper() {}

    public static MedicalTimelineEntryResponse toResponse(MedicalRecord m) {
        if (m == null) return null;
        return MedicalTimelineEntryResponse.builder()
                .id(m.getId())
                .recordType(m.getRecordType().name())
                .description(m.getDescription())
                .recordDate(m.getRecordDate())
                .doctorName(m.getAppointment() != null ? m.getAppointment().getDoctor().getFullName() : null)
                .appointmentId(m.getAppointment() != null ? m.getAppointment().getId() : null)
                .build();
    }
}
