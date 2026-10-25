package com.smartcare.repository;

import com.smartcare.entity.Appointment;
import com.smartcare.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentCode(String appointmentCode);

    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);

    List<Appointment> findByFamilyMemberIdOrderByAppointmentDateDescAppointmentTimeDesc(Long familyMemberId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusIn(
            Long doctorId, LocalDate appointmentDate, List<AppointmentStatus> statuses);

    List<Appointment> findByPatientIdAndStatusIn(Long patientId, List<AppointmentStatus> statuses);

    // Double-booking guard used by the service layer before/inside the
    // booking transaction (Business Rule #1).
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
            Long doctorId, LocalDate appointmentDate, java.time.LocalTime appointmentTime,
            List<AppointmentStatus> excludedStatuses);

    // Patients who asked to be notified about earlier slots for a given
    // doctor and are still waiting (Module 13 - cancellation slot recovery).
    List<Appointment> findByDoctorIdAndNotifyOnEarlierSlotTrueAndStatusIn(
            Long doctorId, List<AppointmentStatus> statuses);

    long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // Used by the no-show risk rule engine (Module 12).
    long countByPatientIdAndStatusAndAppointmentDateAfter(
            Long patientId, AppointmentStatus status, LocalDate afterDate);

    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.patient.id = :patientId AND a.status = :status
            """)
    long countPatientAppointmentsByStatus(@Param("patientId") Long patientId,
                                           @Param("status") AppointmentStatus status);

    // Admin analytics (Module 18): counts per status for a date range.
    @Query("""
            SELECT a.status, COUNT(a) FROM Appointment a
            WHERE a.appointmentDate BETWEEN :from AND :to
            GROUP BY a.status
            """)
    List<Object[]> countByStatusBetweenDates(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT a.department.name, COUNT(a) FROM Appointment a
            WHERE a.appointmentDate BETWEEN :from AND :to
            GROUP BY a.department.name
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> mostBookedDepartments(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT a.doctor.id, a.doctor.fullName, COUNT(a) FROM Appointment a
            WHERE a.appointmentDate BETWEEN :from AND :to
            GROUP BY a.doctor.id, a.doctor.fullName
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> doctorUtilization(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<Appointment> findByStatusAndAppointmentDate(AppointmentStatus status, LocalDate appointmentDate);
}
