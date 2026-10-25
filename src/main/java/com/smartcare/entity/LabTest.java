package com.smartcare.entity;

import com.smartcare.entity.enums.LabTestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Lab test requested by a doctor during/after a consultation (Module 16/15).
 */
@Getter
@Setter
@Entity
@Table(name = "lab_tests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LabTest extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_doctor_id", nullable = false)
    private Doctor requestedBy;

    @NotBlank
    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LabTestStatus status = LabTestStatus.REQUESTED;

    @Column(name = "requested_date", nullable = false)
    @Builder.Default
    private LocalDate requestedDate = LocalDate.now();

    @Column(name = "result_notes", length = 2000)
    private String resultNotes;
}
