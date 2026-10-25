package com.smartcare.controller;

import com.smartcare.dto.labtest.LabTestRequest;
import com.smartcare.dto.labtest.LabTestResponse;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.DoctorService;
import com.smartcare.service.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Doctor "request lab tests" feature + admin/lab result updates. */
@RestController
@RequestMapping("/api/lab-tests")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;
    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LabTestResponse> request(@Valid @RequestBody LabTestRequest request) {
        Long doctorId = doctorService.getByUserId(SecurityUtils.getCurrentUserId()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(labTestService.request(doctorId, request));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<List<LabTestResponse>> getForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(labTestService.getForPatient(patientId));
    }

    @PutMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<LabTestResponse> updateResult(@PathVariable Long id,
                                                         @RequestParam(required = false) String resultNotes,
                                                         @RequestParam(required = false) String status) {
        return ResponseEntity.ok(labTestService.updateResult(id, resultNotes, status));
    }
}
