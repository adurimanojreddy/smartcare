package com.smartcare.service.impl;

import com.smartcare.dto.auth.DoctorCreateRequest;
import com.smartcare.dto.auth.JwtAuthResponse;
import com.smartcare.dto.auth.LoginRequest;
import com.smartcare.dto.auth.RegisterPatientRequest;
import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.entity.Department;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.Patient;
import com.smartcare.entity.User;
import com.smartcare.entity.enums.DoctorStatus;
import com.smartcare.entity.enums.Gender;
import com.smartcare.entity.enums.Role;
import com.smartcare.exception.DepartmentNotFoundException;
import com.smartcare.exception.DuplicateResourceException;
import com.smartcare.mapper.DoctorMapper;
import com.smartcare.repository.DepartmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.PatientRepository;
import com.smartcare.repository.UserRepository;
import com.smartcare.security.JwtTokenProvider;
import com.smartcare.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public JwtAuthResponse registerPatient(RegisterPatientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.ROLE_PATIENT)
                .enabled(true)
                .accountNonLocked(true)
                .build();
        user = userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(parseGender(request.getGender()))
                .phone(request.getPhone())
                .email(request.getEmail())
                .registrationDate(LocalDate.now())
                .build();
        patientRepository.save(patient);

        return issueToken(user, request.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public JwtAuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new BadCredentialsException("This account is disabled. Please contact support.");
        }

        String displayName = resolveDisplayName(user);
        return issueToken(user, displayName);
    }

    @Override
    @Transactional
    public DoctorResponse createDoctor(DoctorCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getTemporaryPassword()))
                .phone(request.getPhone())
                .role(Role.ROLE_DOCTOR)
                .enabled(true)
                .accountNonLocked(true)
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .fullName(request.getFullName())
                .specialization(request.getSpecialization())
                .department(department)
                .qualification(request.getQualification())
                .experienceYears(request.getExperienceYears())
                .consultationFee(request.getConsultationFee())
                .consultationDurationMinutes(
                        request.getConsultationDurationMinutes() != null
                                ? request.getConsultationDurationMinutes() : 15)
                .status(DoctorStatus.ACTIVE)
                .build();
        doctor = doctorRepository.save(doctor);

        return DoctorMapper.toResponse(doctor);
    }

    private JwtAuthResponse issueToken(User user, String displayName) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return JwtAuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenProvider.getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(displayName)
                .role(user.getRole().name())
                .build();
    }

    private String resolveDisplayName(User user) {
        switch (user.getRole()) {
            case ROLE_PATIENT:
                return patientRepository.findByUserId(user.getId())
                        .map(Patient::getFullName).orElse(user.getEmail());
            case ROLE_DOCTOR:
                return doctorRepository.findByUserId(user.getId())
                        .map(Doctor::getFullName).orElse(user.getEmail());
            default:
                return "Admin";
        }
    }

    private Gender parseGender(String gender) {
        if (gender == null || gender.isBlank()) return null;
        try {
            return Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
