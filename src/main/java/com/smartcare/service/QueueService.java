package com.smartcare.service;

import com.smartcare.dto.queue.QueueEntryResponse;
import com.smartcare.dto.queue.QueueStatusResponse;
import com.smartcare.entity.Appointment;

import java.util.List;

public interface QueueService {

    /** Creates the WAITING queue entry for a just-checked-in appointment and assigns its token number. */
    int joinQueue(Appointment appointment);

    QueueStatusResponse getStatusForAppointment(Long appointmentId);

    List<QueueEntryResponse> getQueueForDoctorToday(Long doctorId);

    QueueEntryResponse callNext(Long doctorId);

    QueueEntryResponse startConsultation(Long appointmentId, Long doctorId);

    QueueEntryResponse completeConsultation(Long appointmentId, Long doctorId);

    QueueEntryResponse skipPatient(Long appointmentId, Long doctorId);

    QueueEntryResponse markNoShow(Long appointmentId, Long doctorId);
}
