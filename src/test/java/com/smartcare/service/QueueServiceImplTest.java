package com.smartcare.service;

import com.smartcare.dto.queue.QueueEntryResponse;
import com.smartcare.dto.queue.QueueStatusResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.Patient;
import com.smartcare.entity.Queue;
import com.smartcare.entity.enums.ConsultationType;
import com.smartcare.entity.enums.QueueStatus;
import com.smartcare.exception.InvalidAppointmentException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.QueueRepository;
import com.smartcare.service.impl.QueueServiceImpl;
import static com.smartcare.TestEntities.withId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Module 27 - "Queue tests". Covers Business Rules #5, #6, #7. */
@ExtendWith(MockitoExtension.class)
class QueueServiceImplTest {

    @Mock private QueueRepository queueRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private QueueServiceImpl queueService;

    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        doctor = withId(Doctor.builder().fullName("Kumar").consultationDurationMinutes(15).build(), 10L);
        Patient patient = withId(Patient.builder().fullName("Asha Rao").build(), 100L);
        appointment = withId(Appointment.builder()
                .doctor(doctor).patient(patient)
                .appointmentDate(LocalDate.now())
                .consultationType(ConsultationType.IN_PERSON)
                .build(), 1L);
    }

    @Test
    void joinQueue_firstPatientOfDay_getsTokenOne_businessRule6() {
        when(queueRepository.findTopByDoctorIdAndQueueDateOrderByTokenNumberDesc(10L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(queueRepository.save(any(Queue.class))).thenAnswer(inv -> inv.getArgument(0));

        int token = queueService.joinQueue(appointment);

        assertThat(token).isEqualTo(1);
    }

    @Test
    void joinQueue_subsequentPatient_incrementsFromLastToken_businessRule6() {
        Queue previous = Queue.builder().tokenNumber(7).build();
        when(queueRepository.findTopByDoctorIdAndQueueDateOrderByTokenNumberDesc(10L, LocalDate.now()))
                .thenReturn(Optional.of(previous));
        when(queueRepository.save(any(Queue.class))).thenAnswer(inv -> inv.getArgument(0));

        int token = queueService.joinQueue(appointment);

        assertThat(token).isEqualTo(8);
    }

    @Test
    void getStatusForAppointment_computesPatientsAheadAndWaitEstimate() {
        Queue queue = Queue.builder().appointment(appointment).doctor(doctor)
                .queueDate(LocalDate.now()).tokenNumber(5).status(QueueStatus.WAITING).build();

        when(queueRepository.findByAppointmentId(1L)).thenReturn(Optional.of(queue));
        when(queueRepository.findTopByDoctorIdAndQueueDateAndStatusOrderByCalledAtDesc(
                10L, LocalDate.now(), QueueStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        when(queueRepository.countByDoctorIdAndQueueDateAndStatusAndTokenNumberLessThan(
                10L, LocalDate.now(), QueueStatus.WAITING, 5)).thenReturn(3L);
        when(doctorRepository.findById(10L)).thenReturn(Optional.of(doctor));

        QueueStatusResponse status = queueService.getStatusForAppointment(1L);

        assertThat(status.getYourToken()).isEqualTo(5);
        assertThat(status.getPatientsAhead()).isEqualTo(3);
        assertThat(status.getEstimatedWaitingMinutes()).isEqualTo(45); // 3 * 15 min
    }

    @Test
    void callNext_withNoOneWaiting_throwsInvalidAppointment() {
        when(queueRepository.findByDoctorIdAndQueueDateAndStatusOrderByTokenNumberAsc(
                10L, LocalDate.now(), QueueStatus.WAITING)).thenReturn(List.of());

        assertThatThrownBy(() -> queueService.callNext(10L))
                .isInstanceOf(InvalidAppointmentException.class);
    }

    @Test
    void callNext_callsTheLowestWaitingToken() {
        Queue low = Queue.builder().appointment(appointment).doctor(doctor).tokenNumber(3).status(QueueStatus.WAITING).build();
        Queue high = Queue.builder().appointment(appointment).doctor(doctor).tokenNumber(9).status(QueueStatus.WAITING).build();
        when(queueRepository.findByDoctorIdAndQueueDateAndStatusOrderByTokenNumberAsc(
                10L, LocalDate.now(), QueueStatus.WAITING)).thenReturn(List.of(low, high));
        when(queueRepository.save(any(Queue.class))).thenAnswer(inv -> inv.getArgument(0));

        QueueEntryResponse result = queueService.callNext(10L);

        assertThat(result.getTokenNumber()).isEqualTo(3);
        assertThat(result.getStatus()).isEqualTo("CALLED");
    }

    @Test
    void queueAction_forDifferentDoctor_throwsUnauthorized_businessRule7() {
        Queue queue = Queue.builder().appointment(appointment).doctor(doctor)
                .queueDate(LocalDate.now()).tokenNumber(1).status(QueueStatus.WAITING).build();
        when(queueRepository.findByAppointmentId(1L)).thenReturn(Optional.of(queue));

        assertThatThrownBy(() -> queueService.startConsultation(1L, 999L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
