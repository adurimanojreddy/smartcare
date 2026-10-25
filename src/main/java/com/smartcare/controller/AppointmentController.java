package com.smartcare.controller;

import com.smartcare.dto.appointment.AppointmentBookingRequest;
import com.smartcare.dto.appointment.AppointmentResponse;
import com.smartcare.dto.appointment.CancelAppointmentRequest;
import com.smartcare.dto.appointment.CheckInResponse;
import com.smartcare.dto.appointment.RescheduleRequest;
import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.patient.PatientResponse;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.DoctorService;
import com.smartcare.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Module 8/10/20 - Appointments API. */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getByDoctorAndDate(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if ("ROLE_DOCTOR".equals(SecurityUtils.getCurrentUserRole())) {
            DoctorResponse self = doctorService.getByUserId(SecurityUtils.getCurrentUserId());
            if (!self.getId().equals(doctorId)) {
                throw new UnauthorizedAccessException("You may only view your own appointments");
            }
        }
        return ResponseEntity.ok(appointmentService.getForDoctorAndDate(doctorId, date));
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody AppointmentBookingRequest request) {
        Long patientId = currentPatientId();
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(patientId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id) {
        assertCanView(id);
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping("/code/{appointmentCode}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<AppointmentResponse> getByCode(@PathVariable String appointmentCode) {
        return ResponseEntity.ok(appointmentService.getByCode(appointmentCode));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id,
                                                       @RequestBody(required = false) CancelAppointmentRequest request) {
        Long patientId = resolvePatientIdForWrite(id);
        return ResponseEntity.ok(appointmentService.cancel(id, patientId, request));
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public ResponseEntity<AppointmentResponse> reschedule(@PathVariable Long id,
                                                           @Valid @RequestBody RescheduleRequest request) {
        Long patientId = resolvePatientIdForWrite(id);
        return ResponseEntity.ok(appointmentService.reschedule(id, patientId, request));
    }

    /** Module 20's documented shape: check in by the appointment's internal id. */
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<CheckInResponse> checkInById(@PathVariable Long id) {
        String code = appointmentService.getById(id).getAppointmentCode();
        return ResponseEntity.ok(appointmentService.checkIn(code));
    }

    /** Module 10 - the actual QR scan flow: the scanned payload is the appointment code, not the numeric id. */
    @PostMapping("/qr-check-in/{appointmentCode}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<CheckInResponse> checkInByCode(@PathVariable String appointmentCode) {
        return ResponseEntity.ok(appointmentService.checkIn(appointmentCode));
    }

    private Long currentPatientId() {
        PatientResponse self = patientService.getByUserId(SecurityUtils.getCurrentUserId());
        return self.getId();
    }

    /** For admin-initiated writes, we defer ownership entirely to the service (admin can act for any patient). */
    private Long resolvePatientIdForWrite(Long appointmentId) {
        if ("ROLE_ADMIN".equals(SecurityUtils.getCurrentUserRole())) {
            // Admin bypasses the ownership check inside the service by
            // passing the appointment's actual patient id.
            return appointmentService.getById(appointmentId).getPatientId();
        }
        return currentPatientId();
    }

    private void assertCanView(Long appointmentId) {
        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_ADMIN".equals(role)) return;
        if ("ROLE_PATIENT".equals(role)) {
            appointmentService.assertPatientOwnsAppointment(currentPatientId(), appointmentId);
        } else if ("ROLE_DOCTOR".equals(role)) {
            DoctorResponse self = doctorService.getByUserId(SecurityUtils.getCurrentUserId());
            appointmentService.assertDoctorOwnsAppointment(self.getId(), appointmentId);
        }
    }
}
