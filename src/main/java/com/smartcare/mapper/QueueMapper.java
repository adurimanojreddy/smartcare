package com.smartcare.mapper;

import com.smartcare.dto.queue.QueueEntryResponse;
import com.smartcare.entity.Queue;

public final class QueueMapper {
    private QueueMapper() {}

    public static QueueEntryResponse toEntryResponse(Queue q) {
        if (q == null) return null;
        return QueueEntryResponse.builder()
                .queueId(q.getId())
                .appointmentId(q.getAppointment().getId())
                .tokenNumber(q.getTokenNumber())
                .patientName(resolvePatientName(q))
                .status(q.getStatus().name())
                .checkedInAt(q.getCheckedInAt())
                .consultationType(q.getAppointment().getConsultationType().name())
                .build();
    }

    private static String resolvePatientName(Queue q) {
        var appointment = q.getAppointment();
        return appointment.getFamilyMember() != null
                ? appointment.getFamilyMember().getFullName()
                : appointment.getPatient().getFullName();
    }
}
