package com.smartcare.controller;

import com.smartcare.dto.family.FamilyMemberRequest;
import com.smartcare.dto.family.FamilyMemberResponse;
import com.smartcare.dto.medicalrecord.MedicalTimelineEntryResponse;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.FamilyMemberService;
import com.smartcare.service.MedicalRecordService;
import com.smartcare.service.PatientService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Module 14 - Family Account. All endpoints operate on the logged-in patient's own family members. */
@RestController
@RequestMapping("/api/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;
    private final PatientService patientService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<FamilyMemberResponse>> getMine() {
        return ResponseEntity.ok(familyMemberService.getByOwnerPatient(currentPatientId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<FamilyMemberResponse> add(@Valid @RequestBody FamilyMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(familyMemberService.add(currentPatientId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<FamilyMemberResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody FamilyMemberRequest request) {
        return ResponseEntity.ok(familyMemberService.update(currentPatientId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        familyMemberService.delete(currentPatientId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/medical-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalTimelineEntryResponse>> getMedicalHistory(@PathVariable Long id) {
        familyMemberService.assertOwnership(currentPatientId(), id);
        return ResponseEntity.ok(medicalRecordService.getTimelineForFamilyMember(id));
    }

    private Long currentPatientId() {
        return patientService.getByUserId(SecurityUtils.getCurrentUserId()).getId();
    }
}
