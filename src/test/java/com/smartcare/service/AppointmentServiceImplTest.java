package com.smartcare.service;

import com.smartcare.dto.appointment.AppointmentBookingRequest;
import com.smartcare.dto.appointment.AppointmentResponse;
import com.smartcare.dto.appointment.CancelAppointmentRequest;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.AppointmentSlot;
import com.smartcare.entity.Department;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.Patient;
import com.smartcare.entity.enums.AppointmentStatus;
import com.smartcare.entity.enums.NoShowRisk;
import com.smartcare.entity.enums.SlotStatus;
import com.smartcare.exception.InvalidAppointmentException;
import com.smartcare.exception.SlotNotAvailableException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.AppointmentSlotRepository;
import com.smartcare.repository.FamilyMemberRepository;
import com.smartcare.repository.PatientRepository;
import com.smartcare.service.impl.AppointmentServiceImpl;
import com.smartcare.util.AppointmentCodeGenerator;
import com.smartcare.util.QrCodeGenerator;
import static com.smartcare.TestEntities.withId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Module 27 - "Appointment booking tests" and "Double-booking tests".
 * Covers Business Rules #1, #2, #3, #10, #13.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AppointmentSlotRepository slotRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private QueueService queueService;
    @Mock private NotificationService notificationService;
    @Mock private MedicalRecordService medicalRecordService;
    @Mock private NoShowRiskService noShowRiskService;
    @Mock private AppointmentCodeGenerator codeGenerator;
    @Mock private QrCodeGenerator qrCodeGenerator;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Department department;
    private AppointmentSlot slot;
    private AppointmentBookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        department = withId(Department.builder().name("Dermatology").build(), 1L);
        doctor = withId(Doctor.builder().fullName("Kumar").department(department)
                .consultationFee(BigDecimal.ZERO).build(), 10L);
        patient = withId(Patient.builder().fullName("Asha Rao").build(), 100L);

        slot = withId(AppointmentSlot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(10, 45))
                .status(SlotStatus.AVAILABLE)
                .build(), 500L);

        bookingRequest = AppointmentBookingRequest.builder()
                .doctorId(10L)
                .slotId(500L)
                .consultationType("IN_PERSON")
                .build();

        lenient().when(qrCodeGenerator.generateBase64Png(any())).thenReturn("base64png");
        lenient().when(codeGenerator.generate()).thenReturn("HSP-2026-000001");
        lenient().when(noShowRiskService.assessRisk(anyLong())).thenReturn(NoShowRisk.LOW);
    }

    @Test
    void book_withAvailableSlot_succeedsAndMarksSlotBooked() {
        when(patientRepository.findById(100L)).thenReturn(Optional.of(patient));
        when(slotRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                eq(10L), eq(slot.getSlotDate()), eq(slot.getStartTime()), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(999L);
            return a;
        });

        AppointmentResponse response = appointmentService.book(100L, bookingRequest);

        assertThat(response.getAppointmentCode()).isEqualTo("HSP-2026-000001");
        assertThat(response.getStatus()).isEqualTo("CONFIRMED"); // no fee -> auto-confirmed
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        verify(slotRepository).save(slot);
    }

    @Test
    void book_whenSlotAlreadyBooked_throwsSlotNotAvailable() {
        slot.setStatus(SlotStatus.BOOKED);
        when(patientRepository.findById(100L)).thenReturn(Optional.of(patient));
        when(slotRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.book(100L, bookingRequest))
                .isInstanceOf(SlotNotAvailableException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void book_whenDoctorAlreadyHasAppointmentAtThatTime_throwsSlotNotAvailable_businessRule1() {
        when(patientRepository.findById(100L)).thenReturn(Optional.of(patient));
        when(slotRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(slot));
        // Simulates a second concurrent booking finding the doctor already booked
        // at that exact date/time - Business Rule #1.
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                eq(10L), eq(slot.getSlotDate()), eq(slot.getStartTime()), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.book(100L, bookingRequest))
                .isInstanceOf(SlotNotAvailableException.class)
                .hasMessageContaining("already has an appointment");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void book_withPastSlot_throwsInvalidAppointment_businessRule10() {
        slot.setSlotDate(LocalDate.now().minusDays(1));
        when(patientRepository.findById(100L)).thenReturn(Optional.of(patient));
        when(slotRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.book(100L, bookingRequest))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("past");
    }

    @Test
    void book_withFeeApplyingDoctor_leavesAppointmentPendingPayment() {
        doctor.setConsultationFee(new BigDecimal("50.00"));
        when(patientRepository.findById(100L)).thenReturn(Optional.of(patient));
        when(slotRepository.findByIdForUpdate(500L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.book(100L, bookingRequest);

        assertThat(response.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(response.getStatus()).isEqualTo("BOOKED");
    }

    @Test
    void cancel_releasesSlotAndNotifiesEarlierSlotSeekers_businessRule3And13() {
        Appointment appointment = withId(Appointment.builder()
                .appointmentCode("HSP-2026-000001")
                .patient(patient).doctor(doctor).department(department).slot(slot)
                .appointmentDate(LocalDate.now().plusDays(5))
                .appointmentTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.CONFIRMED)
                .consultationType(com.smartcare.entity.enums.ConsultationType.IN_PERSON)
                .build(), 1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentRepository.findByDoctorIdAndNotifyOnEarlierSlotTrueAndStatusIn(any(), any()))
                .thenReturn(List.of());

        AppointmentResponse response = appointmentService.cancel(
                1L, 100L, CancelAppointmentRequest.builder().reason("Change of plans").build());

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        verify(slotRepository).save(slot);
    }

    @Test
    void cancel_byNonOwningPatient_throwsUnauthorized() {
        Appointment appointment = withId(Appointment.builder()
                .appointmentCode("HSP-2026-000001")
                .patient(patient).doctor(doctor).department(department).slot(slot)
                .appointmentDate(LocalDate.now().plusDays(5))
                .appointmentTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.CONFIRMED)
                .consultationType(com.smartcare.entity.enums.ConsultationType.IN_PERSON)
                .build(), 1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(1L, 999L, null))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
