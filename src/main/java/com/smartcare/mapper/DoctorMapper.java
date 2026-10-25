package com.smartcare.mapper;

import com.smartcare.dto.doctor.DoctorResponse;
import com.smartcare.dto.doctor.DoctorScheduleResponse;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.DoctorSchedule;

public final class DoctorMapper {
    private DoctorMapper() {}

    public static DoctorResponse toResponse(Doctor d) {
        if (d == null) return null;
        return DoctorResponse.builder()
                .id(d.getId())
                .fullName(d.getFullName())
                .email(d.getUser() != null ? d.getUser().getEmail() : null)
                .phone(d.getUser() != null ? d.getUser().getPhone() : null)
                .specialization(d.getSpecialization())
                .departmentId(d.getDepartment() != null ? d.getDepartment().getId() : null)
                .departmentName(d.getDepartment() != null ? d.getDepartment().getName() : null)
                .qualification(d.getQualification())
                .experienceYears(d.getExperienceYears())
                .consultationFee(d.getConsultationFee())
                .consultationDurationMinutes(d.getConsultationDurationMinutes())
                .status(d.getStatus() != null ? d.getStatus().name() : null)
                .profilePhotoUrl(d.getProfilePhotoUrl())
                .build();
    }

    public static DoctorScheduleResponse toScheduleResponse(DoctorSchedule s) {
        if (s == null) return null;
        return DoctorScheduleResponse.builder()
                .id(s.getId())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .active(s.isActive())
                .build();
    }
}
