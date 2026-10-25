package com.smartcare.service;

import com.smartcare.dto.auth.JwtAuthResponse;
import com.smartcare.dto.auth.LoginRequest;
import com.smartcare.dto.auth.RegisterPatientRequest;
import com.smartcare.entity.Patient;
import com.smartcare.entity.User;
import com.smartcare.entity.enums.Role;
import com.smartcare.exception.DuplicateResourceException;
import com.smartcare.repository.DepartmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.PatientRepository;
import com.smartcare.repository.UserRepository;
import com.smartcare.security.JwtTokenProvider;
import com.smartcare.service.impl.AuthServiceImpl;
import static com.smartcare.TestEntities.withId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Module 27 - "Authentication tests". Pure Mockito unit tests: no Spring
 * context, no database - just the service logic and its collaborators.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterPatientRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterPatientRequest.builder()
                .fullName("Asha Rao")
                .email("asha@example.com")
                .password("Password123")
                .phone("9998887777")
                .build();
    }

    @Test
    void registerPatient_createsUserAndPatient_andReturnsToken() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");

        User savedUser = withId(User.builder().email("asha@example.com").role(Role.ROLE_PATIENT).build(), 1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(1L, "asha@example.com", "ROLE_PATIENT")).thenReturn("fake-jwt");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        JwtAuthResponse response = authService.registerPatient(registerRequest);

        assertThat(response.getAccessToken()).isEqualTo("fake-jwt");
        assertThat(response.getRole()).isEqualTo("ROLE_PATIENT");
        assertThat(response.getFullName()).isEqualTo("Asha Rao");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void registerPatient_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerPatient(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void login_withCorrectCredentials_returnsToken() {
        User user = withId(User.builder().email("doc@example.com").password("hashed").role(Role.ROLE_DOCTOR)
                .enabled(true).accountNonLocked(true).build(), 5L);
        when(userRepository.findByEmail("doc@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext", "hashed")).thenReturn(true);
        when(doctorRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(jwtTokenProvider.generateToken(5L, "doc@example.com", "ROLE_DOCTOR")).thenReturn("doc-jwt");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        LoginRequest loginRequest = LoginRequest.builder().email("doc@example.com").password("plaintext").build();
        JwtAuthResponse response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("doc-jwt");
        assertThat(response.getRole()).isEqualTo("ROLE_DOCTOR");
    }

    @Test
    void login_withWrongPassword_throwsBadCredentials() {
        User user = withId(User.builder().email("doc@example.com").password("hashed").role(Role.ROLE_DOCTOR)
                .enabled(true).accountNonLocked(true).build(), 5L);
        when(userRepository.findByEmail("doc@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest loginRequest = LoginRequest.builder().email("doc@example.com").password("wrong").build();

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_withUnknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        LoginRequest loginRequest = LoginRequest.builder().email("nobody@example.com").password("x").build();

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
