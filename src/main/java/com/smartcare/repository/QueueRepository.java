package com.smartcare.repository;

import com.smartcare.entity.Queue;
import com.smartcare.entity.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QueueRepository extends JpaRepository<Queue, Long> {

    Optional<Queue> findByAppointmentId(Long appointmentId);

    List<Queue> findByDoctorIdAndQueueDateOrderByTokenNumberAsc(Long doctorId, LocalDate queueDate);

    List<Queue> findByDoctorIdAndQueueDateAndStatusOrderByTokenNumberAsc(
            Long doctorId, LocalDate queueDate, QueueStatus status);

    // Last token issued today for this doctor, to compute the next token number.
    Optional<Queue> findTopByDoctorIdAndQueueDateOrderByTokenNumberDesc(Long doctorId, LocalDate queueDate);

    // Currently being served, to show "Current Token: 12" (Module 11).
    Optional<Queue> findTopByDoctorIdAndQueueDateAndStatusOrderByCalledAtDesc(
            Long doctorId, LocalDate queueDate, QueueStatus status);

    long countByDoctorIdAndQueueDateAndStatus(Long doctorId, LocalDate queueDate, QueueStatus status);

    // Hospital-wide count across all doctors, for the admin dashboard (Module 18).
    long countByQueueDateAndStatus(LocalDate queueDate, QueueStatus status);

    // Patients ahead of a given token in the WAITING state (Module 11).
    long countByDoctorIdAndQueueDateAndStatusAndTokenNumberLessThan(
            Long doctorId, LocalDate queueDate, QueueStatus status, Integer tokenNumber);
}
