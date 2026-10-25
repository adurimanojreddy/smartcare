package com.smartcare.mapper;

import com.smartcare.dto.labtest.LabTestResponse;
import com.smartcare.entity.LabTest;

public final class LabTestMapper {
    private LabTestMapper() {}

    public static LabTestResponse toResponse(LabTest l) {
        if (l == null) return null;
        return LabTestResponse.builder()
                .id(l.getId())
                .testName(l.getTestName())
                .status(l.getStatus().name())
                .requestedDate(l.getRequestedDate())
                .resultNotes(l.getResultNotes())
                .requestedByDoctorName(l.getRequestedBy().getFullName())
                .build();
    }
}
