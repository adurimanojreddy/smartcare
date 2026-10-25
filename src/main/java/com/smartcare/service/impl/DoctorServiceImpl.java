package com.smartcare.service.impl;

import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.doctor.DoctorUpdateRequest;
import com.smartcare.entity.Department;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.enums.DoctorStatus;
import com.smartcare.exception.DepartmentNotFoundException;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.mapper.DoctorMapper;
import com.smartcare.repository.DepartmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAll() {
        return doctorRepository.findAll().stream().map(DoctorMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long doctorId) {
        return DoctorMapper.toResponse(findOrThrow(doctorId));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found for this account"));
        return DoctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> search(String name, String specialization, Long departmentId) {
        String namePattern = blankToNull(name);
        String specializationPattern = blankToNull(specialization);
        return doctorRepository.searchDoctors(namePattern, specializationPattern, departmentId).stream()
                .map(DoctorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DoctorResponse update(Long doctorId, DoctorUpdateRequest request) {
        Doctor doctor = findOrThrow(doctorId);

        if (request.getFullName() != null) doctor.setFullName(request.getFullName());
        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new DepartmentNotFoundException(
                            "Department not found with id: " + request.getDepartmentId()));
            doctor.setDepartment(department);
        }
        if (request.getQualification() != null) doctor.setQualification(request.getQualification());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
        if (request.getConsultationDurationMinutes() != null) {
            doctor.setConsultationDurationMinutes(request.getConsultationDurationMinutes());
        }
        if (request.getStatus() != null) {
            doctor.setStatus(DoctorStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getProfilePhotoUrl() != null) doctor.setProfilePhotoUrl(request.getProfilePhotoUrl());

        return DoctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public void delete(Long doctorId) {
        Doctor doctor = findOrThrow(doctorId);
        // Soft-delete: keep historical appointments/prescriptions intact.
        doctor.setStatus(DoctorStatus.INACTIVE);
        doctorRepository.save(doctor);
    }

    private Doctor findOrThrow(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + doctorId));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
