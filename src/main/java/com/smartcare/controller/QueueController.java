package com.smartcare.controller;

import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.queue.QueueEntryResponse;
import com.smartcare.dto.queue.QueueStatusResponse;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.DoctorService;
import com.smartcare.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Module 11/20 - Live Queue Management. */
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<List<QueueEntryResponse>> getDoctorQueue(@PathVariable Long doctorId) {
        assertSelfOrAdmin(doctorId);
        return ResponseEntity.ok(queueService.getQueueForDoctorToday(doctorId));
    }

    /** Live position for the patient's own checked-in appointment. */
    @GetMapping("/appointment/{appointmentId}/status")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    public ResponseEntity<QueueStatusResponse> getStatus(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(queueService.getStatusForAppointment(appointmentId));
    }

    @PostMapping("/{doctorId}/call-next")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<QueueEntryResponse> callNext(@PathVariable Long doctorId) {
        assertSelfOrAdmin(doctorId);
        return ResponseEntity.ok(queueService.callNext(doctorId));
    }

    @PostMapping("/{appointmentId}/start")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<QueueEntryResponse> startConsultation(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(queueService.startConsultation(appointmentId, resolveDoctorId(appointmentId)));
    }

    @PostMapping("/{appointmentId}/complete")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<QueueEntryResponse> completeConsultation(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(queueService.completeConsultation(appointmentId, resolveDoctorId(appointmentId)));
    }

    @PostMapping("/{appointmentId}/skip")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<QueueEntryResponse> skip(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(queueService.skipPatient(appointmentId, resolveDoctorId(appointmentId)));
    }

    @PostMapping("/{appointmentId}/no-show")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<QueueEntryResponse> markNoShow(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(queueService.markNoShow(appointmentId, resolveDoctorId(appointmentId)));
    }

    /** Resolves the doctorId used for the ownership check inside QueueService.
     *  Admin acts on behalf of whichever doctor the appointment actually belongs to (Business Rule #9). */
    private Long resolveDoctorId(Long appointmentId) {
        if ("ROLE_ADMIN".equals(SecurityUtils.getCurrentUserRole())) {
            return appointmentService.getById(appointmentId).getDoctorId();
        }
        DoctorResponse self = doctorService.getByUserId(SecurityUtils.getCurrentUserId());
        return self.getId();
    }

    private void assertSelfOrAdmin(Long doctorId) {
        if ("ROLE_ADMIN".equals(SecurityUtils.getCurrentUserRole())) return;
        DoctorResponse self = doctorService.getByUserId(SecurityUtils.getCurrentUserId());
        if (!self.getId().equals(doctorId)) {
            throw new UnauthorizedAccessException("You may only manage your own queue");
        }
    }
}
