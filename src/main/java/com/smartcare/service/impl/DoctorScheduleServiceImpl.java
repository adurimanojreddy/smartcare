package com.smartcare.service.impl;

import com.smartcare.dto.doctor.DoctorScheduleRequest;
import com.smartcare.dto.doctor.DoctorScheduleResponse;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.DoctorSchedule;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.InvalidAppointmentException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.mapper.DoctorMapper;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.DoctorScheduleRepository;
import com.smartcare.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorIdAndActiveTrue(doctorId).stream()
                .map(DoctorMapper::toScheduleResponse)
                .toList();
    }

    @Override
    @Transactional
    public DoctorScheduleResponse addOrUpdate(Long doctorId, DoctorScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidAppointmentException("End time must be after start time");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + doctorId));

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(true)
                .build();

        return DoctorMapper.toScheduleResponse(scheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void deactivate(Long doctorId, Long scheduleId) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        if (!schedule.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException("Schedule not found for this doctor");
        }
        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }
}
