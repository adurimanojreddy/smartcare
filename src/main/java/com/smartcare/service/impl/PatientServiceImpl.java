package com.smartcare.service.impl;

import com.smartcare.dto.patient.PatientResponse;
import com.smartcare.dto.patient.PatientUpdateRequest;
import com.smartcare.entity.Patient;
import com.smartcare.entity.enums.Gender;
import com.smartcare.exception.PatientNotFoundException;
import com.smartcare.mapper.PatientMapper;
import com.smartcare.repository.PatientRepository;
import com.smartcare.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getById(Long patientId) {
        return PatientMapper.toResponse(findPatientOrThrow(patientId));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException("Patient profile not found for this account"));
        return PatientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse updateProfile(Long patientId, PatientUpdateRequest request) {
        Patient patient = findPatientOrThrow(patientId);

        if (request.getFullName() != null) patient.setFullName(request.getFullName());
        if (request.getDateOfBirth() != null) patient.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) patient.setGender(parseGender(request.getGender()));
        if (request.getPhone() != null) patient.setPhone(request.getPhone());
        if (request.getAddress() != null) patient.setAddress(request.getAddress());
        if (request.getEmergencyContact() != null) patient.setEmergencyContact(request.getEmergencyContact());
        if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
        if (request.getProfilePhotoUrl() != null) patient.setProfilePhotoUrl(request.getProfilePhotoUrl());

        return PatientMapper.toResponse(patientRepository.save(patient));
    }

    private Patient findPatientOrThrow(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + patientId));
    }

    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
