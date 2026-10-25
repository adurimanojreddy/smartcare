package com.smartcare.controller;

import com.smartcare.dto.auth.DoctorCreateRequest;
import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.doctor.DoctorUpdateRequest;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.AuthService;
import com.smartcare.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Module 20 - Doctors API. */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAll() {
        return ResponseEntity.ok(doctorService.getAll());
    }

    /** Module 5 - search doctors by name / department / specialization. */
    @GetMapping("/search")
    public ResponseEntity<List<DoctorResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(doctorService.search(name, specialization, departmentId));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorResponse> getMyProfile() {
        return ResponseEntity.ok(doctorService.getByUserId(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    /** Admin-only: create a doctor's User + Doctor profile (Module 1). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody DoctorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createDoctor(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<DoctorResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody DoctorUpdateRequest request) {
        assertSelfOrAdmin(id, request);
        return ResponseEntity.ok(doctorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void assertSelfOrAdmin(Long doctorId, DoctorUpdateRequest request) {
        if ("ROLE_ADMIN".equals(SecurityUtils.getCurrentUserRole())) return;
        DoctorResponse self = doctorService.getByUserId(SecurityUtils.getCurrentUserId());
        if (!self.getId().equals(doctorId)) {
            throw new UnauthorizedAccessException("You may only update your own doctor profile");
        }
        // A doctor should not be able to self-promote/demote their own status.
        request.setStatus(null);
    }
}
