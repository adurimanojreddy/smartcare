package com.smartcare.service;

import com.smartcare.dto.slot.SlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface SlotService {

    /**
     * Generates AppointmentSlot rows for a doctor between fromDate and
     * toDate (inclusive) from their active DoctorSchedule templates,
     * skipping dates/times that already have a slot. Idempotent to call
     * repeatedly (e.g. from a nightly job).
     */
    int generateSlots(Long doctorId, LocalDate fromDate, LocalDate toDate);

    List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date);

    SlotResponse getById(Long slotId);
}
