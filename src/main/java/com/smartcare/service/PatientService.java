package com.smartcare.service;

import com.smartcare.dto.patient.PatientResponse;
import com.smartcare.dto.patient.PatientUpdateRequest;

public interface PatientService {

    PatientResponse getById(Long patientId);

    PatientResponse getByUserId(Long userId);

    PatientResponse updateProfile(Long patientId, PatientUpdateRequest request);
}
