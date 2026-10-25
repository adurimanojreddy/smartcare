package com.smartcare.controller;

import com.smartcare.dto.appointment.AppointmentResponse;
import com.smartcare.dto.medicalrecord.MedicalTimelineEntryResponse;
import com.smartcare.dto.patient.PatientResponse;
import com.smartcare.dto.patient.PatientUpdateRequest;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.MedicalRecordService;
import com.smartcare.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Module 20 - Patients API. */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> getMyProfile() {
        return ResponseEntity.ok(patientService.getByUserId(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        assertSelfOrAdmin(id);
        return ResponseEntity.ok(patientService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<PatientResponse> updateProfile(@PathVariable Long id,
                                                          @Valid @RequestBody PatientUpdateRequest request) {
        assertSelfOrAdmin(id);
        return ResponseEntity.ok(patientService.updateProfile(id, request));
    }

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(@PathVariable Long id) {
        assertSelfOrAdmin(id);
        return ResponseEntity.ok(appointmentService.getForPatient(id));
    }

    @GetMapping("/{id}/medical-history")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<List<MedicalTimelineEntryResponse>> getMedicalHistory(@PathVariable Long id) {
        // Business Rule #8 - patients can only access their own medical information.
        // Doctors are trusted at the controller level here; a finer "is this
        // doctor currently treating this patient" check can be layered in later.
        if (isPatientRole()) {
            assertSelfOrAdmin(id);
        }
        return ResponseEntity.ok(medicalRecordService.getTimelineForPatient(id));
    }

    private void assertSelfOrAdmin(Long patientId) {
        if (isAdmin()) return;
        PatientResponse self = patientService.getByUserId(SecurityUtils.getCurrentUserId());
        if (!self.getId().equals(patientId)) {
            throw new UnauthorizedAccessException("You may only access your own patient record");
        }
    }

    private boolean isAdmin() {
        return "ROLE_ADMIN".equals(SecurityUtils.getCurrentUserRole());
    }

    private boolean isPatientRole() {
        return "ROLE_PATIENT".equals(SecurityUtils.getCurrentUserRole());
    }
}
