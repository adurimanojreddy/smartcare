package com.smartcare.service;

import com.smartcare.dto.appointment.AppointmentBookingRequest;
import com.smartcare.dto.appointment.AppointmentResponse;
import com.smartcare.dto.appointment.CancelAppointmentRequest;
import com.smartcare.dto.appointment.CheckInResponse;
import com.smartcare.dto.appointment.RescheduleRequest;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    /** Module 8 - full booking flow with slot locking (Business Rules #1/#2/#13). */
    AppointmentResponse book(Long patientId, AppointmentBookingRequest request);

    AppointmentResponse getById(Long appointmentId);

    AppointmentResponse getByCode(String appointmentCode);

    List<AppointmentResponse> getForPatient(Long patientId);

    List<AppointmentResponse> getForDoctorAndDate(Long doctorId, LocalDate date);

    AppointmentResponse cancel(Long appointmentId, Long requestingPatientId, CancelAppointmentRequest request);

    AppointmentResponse reschedule(Long appointmentId, Long requestingPatientId, RescheduleRequest request);

    /** Module 10 - QR check-in. Returns the queue/token summary shown to the patient. */
    CheckInResponse checkIn(String appointmentCode);

    /** Called by PaymentService once a payment succeeds/fails (Business Rule #12). */
    void applyPaymentResult(Long appointmentId, boolean success);

    void assertPatientOwnsAppointment(Long patientId, Long appointmentId);

    void assertDoctorOwnsAppointment(Long doctorId, Long appointmentId);
}
