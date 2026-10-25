package com.smartcare.service;

import com.smartcare.dto.doctor.DoctorScheduleRequest;
import com.smartcare.dto.doctor.DoctorScheduleResponse;

import java.util.List;

public interface DoctorScheduleService {

    List<DoctorScheduleResponse> getByDoctor(Long doctorId);

    DoctorScheduleResponse addOrUpdate(Long doctorId, DoctorScheduleRequest request);

    void deactivate(Long doctorId, Long scheduleId);
}
