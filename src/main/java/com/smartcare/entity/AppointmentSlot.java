package com.smartcare.entity;

import com.smartcare.entity.enums.SlotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A concrete, bookable slot for one doctor on one date.
 *
 * The unique constraint on (doctor, slot_date, start_time) plus the
 * @Version optimistic-locking column are the two mechanisms that back
 * Business Rules #1/#2/#13 (no double-booking, transactional slot
 * allocation) at the database level - the service layer still wraps
 * booking in a @Transactional block, but this is the last line of defense.
 */
@Getter
@Setter
@Entity
@Table(
        name = "appointment_slots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doctor_date_start_time",
                columnNames = {"doctor_id", "slot_date", "start_time"}
        )
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AppointmentSlot extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    // Optimistic locking guards against two concurrent booking requests
    // both reading the slot as AVAILABLE and both succeeding.
    @Version
    private Long version;
}
