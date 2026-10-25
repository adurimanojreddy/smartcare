package com.smartcare.controller;

import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.doctor.DoctorScheduleRequest;
import com.smartcare.dto.doctor.DoctorScheduleResponse;
import com.smartcare.dto.slot.SlotResponse;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.security.SecurityUtils;
import com.smartcare.service.DoctorScheduleService;
import com.smartcare.service.DoctorService;
import com.smartcare.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Doctor availability management (Module: DOCTOR features) + slot generation/browsing (Module 7). */
@RestController
@RequestMapping("/api/doctors/{doctorId}")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;
    private final SlotService slotService;
    private final DoctorService doctorService;

    @GetMapping("/schedules")
    public ResponseEntity<List<DoctorScheduleResponse>> getSchedules(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getByDoctor(doctorId));
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<DoctorScheduleResponse> addSchedule(@PathVariable Long doctorId,
                                                               @Valid @RequestBody DoctorScheduleRequest request) {
        assertSelfOrAdmin(doctorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.addOrUpdate(doctorId, request));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Void> deactivateSchedule(@PathVariable Long doctorId, @PathVariable Long scheduleId) {
        assertSelfOrAdmin(doctorId);
        scheduleService.deactivate(doctorId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    /** Generates concrete AppointmentSlot rows from the doctor's weekly schedule for a date range. */
    @PostMapping("/slots/generate")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Map<String, Integer>> generateSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        assertSelfOrAdmin(doctorId);
        int created = slotService.generateSlots(doctorId, from, to);
        return ResponseEntity.ok(Map.of("slotsCreated", created));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getAvailableSlots(doctorId, date));
    }

    private void assertSelfOrAdmin(Long doctorId) {
        if ("ROLE_ADMIN".equals(SecurityUtils.getCurrentUserRole())) return;
        DoctorResponse self = doctorService.getByUserId(SecurityUtils.getCurrentUserId());
        if (!self.getId().equals(doctorId)) {
            throw new UnauthorizedAccessException("You may only manage your own schedule");
        }
    }
}
