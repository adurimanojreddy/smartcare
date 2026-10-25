package com.smartcare.repository;

import com.smartcare.entity.AppointmentSlot;
import com.smartcare.entity.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByDoctorIdAndSlotDateAndStatusOrderByStartTimeAsc(
            Long doctorId, LocalDate slotDate, SlotStatus status);

    List<AppointmentSlot> findByDoctorIdAndSlotDateOrderByStartTimeAsc(Long doctorId, LocalDate slotDate);

    // Earliest available slot on or after a given date - backs the smart
    // recommendation "Earliest Available Appointment" feature (Module 6/7).
    @Query("""
            SELECT s FROM AppointmentSlot s
            WHERE s.doctor.id = :doctorId
              AND s.status = com.smartcare.entity.enums.SlotStatus.AVAILABLE
              AND (s.slotDate > :fromDate OR (s.slotDate = :fromDate AND s.startTime >= :fromTime))
            ORDER BY s.slotDate ASC, s.startTime ASC
            """)
    List<AppointmentSlot> findEarliestAvailable(@Param("doctorId") Long doctorId,
                                                 @Param("fromDate") LocalDate fromDate,
                                                 @Param("fromTime") LocalTime fromTime);

    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);

    // Pessimistic lock used inside the booking transaction to guarantee only
    // one concurrent request can claim a given slot (Business Rule #2/#13,
    // on top of the entity's own @Version optimistic lock).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AppointmentSlot s WHERE s.id = :id")
    Optional<AppointmentSlot> findByIdForUpdate(@Param("id") Long id);
}
