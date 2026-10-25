package com.smartcare.service.impl;

import com.smartcare.dto.appointment.AppointmentBookingRequest;
import com.smartcare.dto.appointment.AppointmentResponse;
import com.smartcare.dto.appointment.CancelAppointmentRequest;
import com.smartcare.dto.appointment.CheckInResponse;
import com.smartcare.dto.appointment.RescheduleRequest;
import com.smartcare.dto.queue.QueueStatusResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.AppointmentSlot;
import com.smartcare.entity.Department;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.FamilyMember;
import com.smartcare.entity.Patient;
import com.smartcare.entity.enums.AppointmentStatus;
import com.smartcare.entity.enums.ConsultationType;
import com.smartcare.entity.enums.MedicalRecordType;
import com.smartcare.entity.enums.NoShowRisk;
import com.smartcare.entity.enums.NotificationType;
import com.smartcare.entity.enums.PaymentStatus;
import com.smartcare.entity.enums.SlotStatus;
import com.smartcare.exception.AppointmentNotFoundException;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.InvalidAppointmentException;
import com.smartcare.exception.PatientNotFoundException;
import com.smartcare.exception.PaymentException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.exception.SlotNotAvailableException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.mapper.AppointmentMapper;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.AppointmentSlotRepository;
import com.smartcare.repository.FamilyMemberRepository;
import com.smartcare.repository.PatientRepository;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.MedicalRecordService;
import com.smartcare.service.NoShowRiskService;
import com.smartcare.service.NotificationService;
import com.smartcare.service.QueueService;
import com.smartcare.util.AppointmentCodeGenerator;
import com.smartcare.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 8 - Appointment Booking, plus cancellation, reschedule, and
 * QR check-in (Module 10). Slot allocation is guarded by:
 *  - a pessimistic DB lock on the AppointmentSlot row during booking,
 *  - the slot's own @Version optimistic lock,
 *  - the unique (doctor, date, start_time) constraint on AppointmentSlot,
 *  - an explicit double-booking existence check on Appointment.
 * Business Rules #1, #2, #10, #13 all rely on this combination.
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final QueueService queueService;
    private final NotificationService notificationService;
    private final MedicalRecordService medicalRecordService;
    private final NoShowRiskService noShowRiskService;
    private final AppointmentCodeGenerator codeGenerator;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${smartcare.business.cancellation-cutoff-hours:2}")
    private int cancellationCutoffHours;

    @Override
    @Transactional
    public AppointmentResponse book(Long patientId, AppointmentBookingRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + patientId));

        FamilyMember familyMember = null;
        if (request.getFamilyMemberId() != null) {
            familyMember = familyMemberRepository.findById(request.getFamilyMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Family member not found with id: " + request.getFamilyMemberId()));
            if (!familyMember.getOwnerPatient().getId().equals(patientId)) {
                throw new UnauthorizedAccessException(
                        "This family member does not belong to your account");
            }
        }

        // Pessimistic write lock: only one concurrent transaction can hold
        // this row, so two patients racing for the same slot cannot both win.
        AppointmentSlot slot = slotRepository.findByIdForUpdate(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getSlotId()));

        if (!slot.getDoctor().getId().equals(request.getDoctorId())) {
            throw new InvalidAppointmentException("The selected slot does not belong to the selected doctor");
        }
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException("This slot is no longer available");
        }
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
        if (slotDateTime.isBefore(LocalDateTime.now())) {
            // Business Rule #10 - past appointment slots cannot be booked.
            throw new InvalidAppointmentException("Cannot book a slot in the past");
        }

        boolean doctorAlreadyBooked = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                        slot.getDoctor().getId(), slot.getSlotDate(), slot.getStartTime(),
                        List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW));
        if (doctorAlreadyBooked) {
            // Business Rule #1 - a doctor cannot have two appointments at the same time.
            throw new SlotNotAvailableException("This doctor already has an appointment at this time");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        Doctor doctor = slot.getDoctor();
        Department department = doctor.getDepartment();

        ConsultationType consultationType = parseConsultationType(request.getConsultationType());

        boolean feeApplies = doctor.getConsultationFee() != null
                && doctor.getConsultationFee().compareTo(BigDecimal.ZERO) > 0;
        PaymentStatus paymentStatus = feeApplies ? PaymentStatus.PENDING : PaymentStatus.NOT_APPLICABLE;
        AppointmentStatus initialStatus = feeApplies ? AppointmentStatus.BOOKED : AppointmentStatus.CONFIRMED;

        NoShowRisk risk = noShowRiskService.assessRisk(patientId);

        String appointmentCode = codeGenerator.generate();
        String meetingLink = consultationType == ConsultationType.ONLINE
                ? "https://smartcare-meet.example.com/" + appointmentCode
                : null;

        Appointment appointment = Appointment.builder()
                .appointmentCode(appointmentCode)
                .patient(patient)
                .familyMember(familyMember)
                .doctor(doctor)
                .department(department)
                .slot(slot)
                .appointmentDate(slot.getSlotDate())
                .appointmentTime(slot.getStartTime())
                .status(initialStatus)
                .consultationType(consultationType)
                .paymentStatus(paymentStatus)
                .qrCodeData(appointmentCode)
                .meetingLink(meetingLink)
                .noShowRisk(risk)
                .notifyOnEarlierSlot(request.isNotifyOnEarlierSlot())
                .build();

        appointment = appointmentRepository.save(appointment);

        medicalRecordService.addEntry(appointment, MedicalRecordType.APPOINTMENT,
                "Appointment booked with Dr. " + doctor.getFullName() + " (" + department.getName() + ")");

        return toResponseWithQr(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long appointmentId) {
        return toResponseWithQr(findOrThrow(appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getByCode(String appointmentCode) {
        Appointment appointment = appointmentRepository.findByAppointmentCode(appointmentCode)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "No appointment found with code: " + appointmentCode));
        return toResponseWithQr(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getForPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patientId).stream()
                .map(this::toResponseWithQr)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getForDoctorAndDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date).stream()
                .map(this::toResponseWithQr)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponse cancel(Long appointmentId, Long requestingPatientId, CancelAppointmentRequest request) {
        Appointment appointment = findOrThrow(appointmentId);
        assertPatientOwnsAppointment(requestingPatientId, appointmentId);

        if (isTerminalOrInProgress(appointment.getStatus())) {
            throw new InvalidAppointmentException(
                    "Cannot cancel an appointment with status " + appointment.getStatus());
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(), appointment.getAppointmentTime());
        // Business Rule #11 - configurable cancellation cutoff policy.
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN
                && LocalDateTime.now().isAfter(appointmentDateTime.minusHours(cancellationCutoffHours))) {
            throw new InvalidAppointmentException(
                    "Appointments can only be cancelled at least " + cancellationCutoffHours
                            + " hour(s) before the scheduled time");
        }

        // Business Rule #3 - cancelled appointments release their slot.
        AppointmentSlot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(request != null ? request.getReason() : null);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment = appointmentRepository.save(appointment);

        notifyEarlierSlotSeekers(appointment, slot);

        return toResponseWithQr(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(Long appointmentId, Long requestingPatientId, RescheduleRequest request) {
        Appointment appointment = findOrThrow(appointmentId);
        assertPatientOwnsAppointment(requestingPatientId, appointmentId);

        if (appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentException(
                    "Only booked/confirmed appointments can be rescheduled (current status: "
                            + appointment.getStatus() + ")");
        }

        AppointmentSlot newSlot = slotRepository.findByIdForUpdate(request.getNewSlotId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Slot not found with id: " + request.getNewSlotId()));

        if (!newSlot.getDoctor().getId().equals(appointment.getDoctor().getId())) {
            throw new InvalidAppointmentException("The new slot must belong to the same doctor");
        }
        if (newSlot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException("The selected new slot is no longer available");
        }
        LocalDateTime newSlotDateTime = LocalDateTime.of(newSlot.getSlotDate(), newSlot.getStartTime());
        if (newSlotDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Cannot reschedule to a slot in the past");
        }

        boolean doctorAlreadyBooked = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                        newSlot.getDoctor().getId(), newSlot.getSlotDate(), newSlot.getStartTime(),
                        List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW));
        if (doctorAlreadyBooked) {
            throw new SlotNotAvailableException("This doctor already has an appointment at the new time");
        }

        // Release the old slot back into the pool.
        AppointmentSlot oldSlot = appointment.getSlot();
        oldSlot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(oldSlot);

        newSlot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(newSlot);

        appointment.setSlot(newSlot);
        appointment.setAppointmentDate(newSlot.getSlotDate());
        appointment.setAppointmentTime(newSlot.getStartTime());
        appointment = appointmentRepository.save(appointment);

        notifyEarlierSlotSeekers(appointment, oldSlot);

        return toResponseWithQr(appointment);
    }

    @Override
    @Transactional
    public CheckInResponse checkIn(String appointmentCode) {
        Appointment appointment = appointmentRepository.findByAppointmentCode(appointmentCode)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "No appointment found with code: " + appointmentCode));

        if (!appointment.getAppointmentDate().equals(LocalDate.now())) {
            throw new InvalidAppointmentException("Check-in is only allowed on the day of the appointment");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentException(
                    "This appointment cannot be checked in (current status: " + appointment.getStatus() + ")");
        }
        // Business Rule #12 - payment must be verified before confirming paid appointments.
        if (appointment.getPaymentStatus() == PaymentStatus.PENDING) {
            throw new PaymentException("Please complete payment before checking in");
        }

        int token = queueService.joinQueue(appointment);

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setCheckInTime(LocalDateTime.now());
        appointment.setTokenNumber(token);
        appointmentRepository.save(appointment);

        QueueStatusResponse queueStatus = queueService.getStatusForAppointment(appointment.getId());

        return CheckInResponse.builder()
                .appointmentId(appointment.getId())
                .tokenNumber(token)
                .currentToken(queueStatus.getCurrentToken())
                .patientsAhead(queueStatus.getPatientsAhead())
                .estimatedWaitingMinutes(queueStatus.getEstimatedWaitingMinutes())
                .build();
    }

    @Override
    @Transactional
    public void applyPaymentResult(Long appointmentId, boolean success) {
        Appointment appointment = findOrThrow(appointmentId);
        if (success) {
            appointment.setPaymentStatus(PaymentStatus.SUCCESS);
            if (appointment.getStatus() == AppointmentStatus.BOOKED) {
                appointment.setStatus(AppointmentStatus.CONFIRMED);
            }
        } else {
            appointment.setPaymentStatus(PaymentStatus.FAILED);
        }
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public void assertPatientOwnsAppointment(Long patientId, Long appointmentId) {
        Appointment appointment = findOrThrow(appointmentId);
        // Business Rule #8 - patients can only access their own (or their
        // family members') medical/appointment information.
        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedAccessException("This appointment does not belong to your account");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void assertDoctorOwnsAppointment(Long doctorId, Long appointmentId) {
        Appointment appointment = findOrThrow(appointmentId);
        // Business Rule #7 - doctors can only access their assigned patients/appointments.
        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedAccessException("This appointment is not assigned to you");
        }
    }

    // ---------------------------------------------------------------

    private void notifyEarlierSlotSeekers(Appointment justFreedFrom, AppointmentSlot freedSlot) {
        LocalDateTime freedDateTime = LocalDateTime.of(freedSlot.getSlotDate(), freedSlot.getStartTime());

        List<Appointment> candidates = appointmentRepository.findByDoctorIdAndNotifyOnEarlierSlotTrueAndStatusIn(
                freedSlot.getDoctor().getId(), List.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED));

        for (Appointment candidate : candidates) {
            LocalDateTime candidateDateTime = LocalDateTime.of(
                    candidate.getAppointmentDate(), candidate.getAppointmentTime());
            // Only notify patients whose current appointment is later than
            // the newly freed slot (Module 13 - "earlier slot" recovery).
            if (candidateDateTime.isAfter(freedDateTime)) {
                notificationService.create(
                        candidate.getPatient().getUser(),
                        NotificationType.SLOT_RECOVERED,
                        "An earlier appointment slot is now available",
                        "Dr. " + freedSlot.getDoctor().getFullName() + " has an opening on "
                                + freedSlot.getSlotDate() + " at " + freedSlot.getStartTime()
                                + ". Book now before it's taken.",
                        String.valueOf(freedSlot.getId())
                );
            }
        }
    }

    private boolean isTerminalOrInProgress(AppointmentStatus status) {
        return status == AppointmentStatus.IN_PROGRESS
                || status == AppointmentStatus.COMPLETED
                || status == AppointmentStatus.CANCELLED
                || status == AppointmentStatus.NO_SHOW;
    }

    private ConsultationType parseConsultationType(String raw) {
        try {
            return ConsultationType.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new InvalidAppointmentException("consultationType must be IN_PERSON or ONLINE");
        }
    }

    private Appointment findOrThrow(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));
    }

    private AppointmentResponse toResponseWithQr(Appointment appointment) {
        String qrBase64 = qrCodeGenerator.generateBase64Png(appointment.getAppointmentCode());
        return AppointmentMapper.toResponse(appointment, qrBase64);
    }
}
