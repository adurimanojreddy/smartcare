package com.smartcare.service;

import com.smartcare.dto.prescription.PrescriptionCreateRequest;
import com.smartcare.dto.prescription.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {

    /** Module 16 - doctor creates a digital prescription for a completed/ongoing appointment. */
    PrescriptionResponse create(Long doctorId, PrescriptionCreateRequest request);

    PrescriptionResponse getById(Long prescriptionId);

    PrescriptionResponse getByAppointment(Long appointmentId);

    List<PrescriptionResponse> getForPatient(Long patientId);
}
