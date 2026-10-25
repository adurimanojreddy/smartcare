package com.smartcare.controller;

import com.smartcare.dto.prescription.PrescriptionCreateRequest;
import com.smartcare.dto.prescription.PrescriptionResponse;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.DoctorService;
import com.smartcare.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Module 16/20 - Digital Prescriptions. */
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> create(@Valid @RequestBody PrescriptionCreateRequest request) {
        Long doctorId = doctorService.getByUserId(SecurityUtils.getCurrentUserId()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.create(doctorId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<PrescriptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<PrescriptionResponse> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(prescriptionService.getByAppointment(appointmentId));
    }

    /** Module 20's documented shape: GET /api/prescriptions/{patientId}. */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<List<PrescriptionResponse>> getForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getForPatient(patientId));
    }
}
