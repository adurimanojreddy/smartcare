package com.smartcare.service;

import com.smartcare.dto.auth.DoctorCreateRequest;
import com.smartcare.dto.auth.JwtAuthResponse;
import com.smartcare.dto.auth.LoginRequest;
import com.smartcare.dto.auth.RegisterPatientRequest;
import com.smartcare.dto.doctor.DoctorResponse;

public interface AuthService {

    JwtAuthResponse registerPatient(RegisterPatientRequest request);

    JwtAuthResponse login(LoginRequest request);

    /** Admin-only: creates the doctor's User + Doctor profile in one step (Module 1). */
    DoctorResponse createDoctor(DoctorCreateRequest request);
}
