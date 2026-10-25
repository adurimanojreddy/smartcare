package com.smartcare.mapper;

import com.smartcare.dto.appointment.AppointmentResponse;
import com.smartcare.entity.Appointment;

public final class AppointmentMapper {
    private AppointmentMapper() {}

    public static AppointmentResponse toResponse(Appointment a, String qrCodeImageBase64) {
        if (a == null) return null;
        return AppointmentResponse.builder()
                .id(a.getId())
                .appointmentCode(a.getAppointmentCode())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFullName())
                .familyMemberId(a.getFamilyMember() != null ? a.getFamilyMember().getId() : null)
                .familyMemberName(a.getFamilyMember() != null ? a.getFamilyMember().getFullName() : null)
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFullName())
                .departmentId(a.getDepartment().getId())
                .departmentName(a.getDepartment().getName())
                .appointmentDate(a.getAppointmentDate())
                .appointmentTime(a.getAppointmentTime())
                .status(a.getStatus().name())
                .consultationType(a.getConsultationType().name())
                .paymentStatus(a.getPaymentStatus().name())
                .tokenNumber(a.getTokenNumber())
                .qrCodeImageBase64(qrCodeImageBase64)
                .checkInTime(a.getCheckInTime())
                .meetingLink(a.getMeetingLink())
                .noShowRisk(a.getNoShowRisk() != null ? a.getNoShowRisk().name() : null)
                .notifyOnEarlierSlot(a.isNotifyOnEarlierSlot())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
