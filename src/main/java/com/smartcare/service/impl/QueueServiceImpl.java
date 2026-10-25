package com.smartcare.service.impl;

import com.smartcare.dto.queue.QueueEntryResponse;
import com.smartcare.dto.queue.QueueStatusResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.Queue;
import com.smartcare.entity.enums.AppointmentStatus;
import com.smartcare.entity.enums.QueueStatus;
import com.smartcare.exception.InvalidAppointmentException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.mapper.QueueMapper;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.QueueRepository;
import com.smartcare.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 11 - Live Queue Management. Token numbers are per doctor/date
 * (Business Rule #6): computed as (last token issued today for this
 * doctor) + 1, so each doctor's numbering restarts every day.
 */
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRepository queueRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    @Value("${smartcare.queue.default-avg-consultation-minutes:10}")
    private int defaultAvgConsultationMinutes;

    @Override
    @Transactional
    public int joinQueue(Appointment appointment) {
        LocalDate today = appointment.getAppointmentDate();
        int nextToken = queueRepository
                .findTopByDoctorIdAndQueueDateOrderByTokenNumberDesc(appointment.getDoctor().getId(), today)
                .map(q -> q.getTokenNumber() + 1)
                .orElse(1);

        Queue queue = Queue.builder()
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .queueDate(today)
                .tokenNumber(nextToken)
                .status(QueueStatus.WAITING)
                .checkedInAt(LocalDateTime.now())
                .build();
        queueRepository.save(queue);
        return nextToken;
    }

    @Override
    @Transactional(readOnly = true)
    public QueueStatusResponse getStatusForAppointment(Long appointmentId) {
        Queue queue = queueRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "This appointment has not checked in yet, so it has no queue position"));

        Integer currentToken = queueRepository
                .findTopByDoctorIdAndQueueDateAndStatusOrderByCalledAtDesc(
                        queue.getDoctor().getId(), queue.getQueueDate(), QueueStatus.IN_PROGRESS)
                .map(Queue::getTokenNumber)
                .orElse(null);

        long patientsAhead = queueRepository.countByDoctorIdAndQueueDateAndStatusAndTokenNumberLessThan(
                queue.getDoctor().getId(), queue.getQueueDate(), QueueStatus.WAITING, queue.getTokenNumber());

        int avgMinutes = resolveAvgConsultationMinutes(queue.getDoctor());
        int estimatedWait = (int) (patientsAhead * avgMinutes);

        return QueueStatusResponse.builder()
                .appointmentId(appointmentId)
                .currentToken(currentToken)
                .yourToken(queue.getTokenNumber())
                .patientsAhead((int) patientsAhead)
                .estimatedWaitingMinutes(estimatedWait)
                .status(queue.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueEntryResponse> getQueueForDoctorToday(Long doctorId) {
        return queueRepository.findByDoctorIdAndQueueDateOrderByTokenNumberAsc(doctorId, LocalDate.now()).stream()
                .map(QueueMapper::toEntryResponse)
                .toList();
    }

    @Override
    @Transactional
    public QueueEntryResponse callNext(Long doctorId) {
        List<Queue> waiting = queueRepository.findByDoctorIdAndQueueDateAndStatusOrderByTokenNumberAsc(
                doctorId, LocalDate.now(), QueueStatus.WAITING);
        if (waiting.isEmpty()) {
            throw new InvalidAppointmentException("No patients are currently waiting in the queue");
        }
        Queue next = waiting.get(0);
        next.setStatus(QueueStatus.CALLED);
        next.setCalledAt(LocalDateTime.now());
        return QueueMapper.toEntryResponse(queueRepository.save(next));
    }

    @Override
    @Transactional
    public QueueEntryResponse startConsultation(Long appointmentId, Long doctorId) {
        Queue queue = findQueueForAppointment(appointmentId, doctorId);
        queue.setStatus(QueueStatus.IN_PROGRESS);
        if (queue.getCalledAt() == null) queue.setCalledAt(LocalDateTime.now());

        Appointment appointment = queue.getAppointment();
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setConsultationStartedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        return QueueMapper.toEntryResponse(queueRepository.save(queue));
    }

    @Override
    @Transactional
    public QueueEntryResponse completeConsultation(Long appointmentId, Long doctorId) {
        Queue queue = findQueueForAppointment(appointmentId, doctorId);
        queue.setStatus(QueueStatus.COMPLETED);
        queue.setCompletedAt(LocalDateTime.now());

        Appointment appointment = queue.getAppointment();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setConsultationCompletedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        return QueueMapper.toEntryResponse(queueRepository.save(queue));
    }

    @Override
    @Transactional
    public QueueEntryResponse skipPatient(Long appointmentId, Long doctorId) {
        Queue queue = findQueueForAppointment(appointmentId, doctorId);
        queue.setStatus(QueueStatus.SKIPPED);
        return QueueMapper.toEntryResponse(queueRepository.save(queue));
    }

    @Override
    @Transactional
    public QueueEntryResponse markNoShow(Long appointmentId, Long doctorId) {
        Queue queue = findQueueForAppointment(appointmentId, doctorId);
        queue.setStatus(QueueStatus.NO_SHOW);

        Appointment appointment = queue.getAppointment();
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(appointment);

        return QueueMapper.toEntryResponse(queueRepository.save(queue));
    }

    private Queue findQueueForAppointment(Long appointmentId, Long doctorId) {
        Queue queue = queueRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No queue entry found for appointment id: " + appointmentId));
        // Business Rule #7 - doctors can only access their assigned patients/appointments.
        if (!queue.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedAccessException("This appointment is not assigned to you");
        }
        return queue;
    }

    private int resolveAvgConsultationMinutes(Doctor doctor) {
        Doctor managed = doctorRepository.findById(doctor.getId()).orElse(doctor);
        return managed.getConsultationDurationMinutes() != null
                ? managed.getConsultationDurationMinutes() : defaultAvgConsultationMinutes;
    }
}
