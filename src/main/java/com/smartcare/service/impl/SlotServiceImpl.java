package com.smartcare.service.impl;

import com.smartcare.dto.slot.SlotResponse;
import com.smartcare.entity.AppointmentSlot;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.DoctorSchedule;
import com.smartcare.entity.enums.SlotStatus;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.mapper.SlotMapper;
import com.smartcare.repository.AppointmentSlotRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.DoctorScheduleRepository;
import com.smartcare.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns a doctor's recurring weekly DoctorSchedule templates into concrete,
 * bookable AppointmentSlot rows (Module 7 - "Doctor availability", "Hospital
 * working hours" inputs to the smart slot recommendation algorithm).
 */
@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private final AppointmentSlotRepository slotRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public int generateSlots(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must not be before fromDate");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + doctorId));

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndActiveTrue(doctorId);
        int durationMinutes = doctor.getConsultationDurationMinutes() != null
                ? doctor.getConsultationDurationMinutes() : 15;

        int created = 0;
        List<AppointmentSlot> toSave = new ArrayList<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            // Business Rule #10 - past slots cannot be booked/generated.
            if (date.isBefore(LocalDate.now())) continue;

            for (DoctorSchedule schedule : schedules) {
                if (schedule.getDayOfWeek() != date.getDayOfWeek()) continue;

                LocalTime cursor = schedule.getStartTime();
                while (cursor.plusMinutes(durationMinutes).compareTo(schedule.getEndTime()) <= 0) {
                    LocalTime slotStart = cursor;
                    LocalTime slotEnd = cursor.plusMinutes(durationMinutes);

                    if (!slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, date, slotStart)) {
                        toSave.add(AppointmentSlot.builder()
                                .doctor(doctor)
                                .slotDate(date)
                                .startTime(slotStart)
                                .endTime(slotEnd)
                                .status(SlotStatus.AVAILABLE)
                                .build());
                        created++;
                    }
                    cursor = slotEnd;
                }
            }
        }

        if (!toSave.isEmpty()) {
            slotRepository.saveAll(toSave);
        }
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        return slotRepository
                .findByDoctorIdAndSlotDateAndStatusOrderByStartTimeAsc(doctorId, date, SlotStatus.AVAILABLE)
                .stream()
                .map(SlotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SlotResponse getById(Long slotId) {
        return slotRepository.findById(slotId)
                .map(SlotMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
    }
}
