package com.smartcare.entity;

import com.smartcare.entity.enums.AppointmentStatus;
import com.smartcare.entity.enums.ConsultationType;
import com.smartcare.entity.enums.NoShowRisk;
import com.smartcare.entity.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Central booking record. See Module 8 for the field list and Module 24
 * for the business rules this entity's constraints/relationships support.
 */
@Getter
@Setter
@Entity
@Table(name = "appointments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Appointment extends BaseEntity {

    // Human-readable public identifier, e.g. HSP-2026-000123 (Module 10)
    @Column(name = "appointment_code", nullable = false, unique = true, length = 30)
    private String appointmentCode;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Null when the appointment is for the account owner themself,
    // populated when booked on behalf of a family member (Module 14).
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id")
    private FamilyMember familyMember;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // One slot maps to exactly one active appointment at a time.
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private AppointmentSlot slot;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", nullable = false, length = 20)
    private ConsultationType consultationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.NOT_APPLICABLE;

    // Per doctor/date/session token, assigned at check-in (Module 10/11).
    @Column(name = "token_number")
    private Integer tokenNumber;

    @Column(name = "qr_code_data", length = 500)
    private String qrCodeData;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "consultation_started_at")
    private LocalDateTime consultationStartedAt;

    @Column(name = "consultation_completed_at")
    private LocalDateTime consultationCompletedAt;

    // Online consultation placeholder link (Module 17) - not a real video platform.
    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "no_show_risk", length = 10)
    @Builder.Default
    private NoShowRisk noShowRisk = NoShowRisk.LOW;

    // Patient opted in to be notified if an earlier slot opens up (Module 13).
    @Builder.Default
    @Column(name = "notify_on_earlier_slot", nullable = false)
    private boolean notifyOnEarlierSlot = false;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
