package com.smartcare.service;

import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.doctor.DoctorUpdateRequest;

import java.util.List;

public interface DoctorService {

    List<DoctorResponse> getAll();

    DoctorResponse getById(Long doctorId);

    DoctorResponse getByUserId(Long userId);

    /** Module 5 - search doctors by name / department / specialization / availability. */
    List<DoctorResponse> search(String name, String specialization, Long departmentId);

    DoctorResponse update(Long doctorId, DoctorUpdateRequest request);

    void delete(Long doctorId);
}
