package com.smartcare.service.impl;

import com.smartcare.dto.prescription.PrescriptionCreateRequest;
import com.smartcare.dto.prescription.PrescriptionMedicineDto;
import com.smartcare.dto.prescription.PrescriptionResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.Prescription;
import com.smartcare.entity.PrescriptionMedicine;
import com.smartcare.entity.enums.MedicalRecordType;
import com.smartcare.exception.AppointmentNotFoundException;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.mapper.PrescriptionMapper;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.PrescriptionRepository;
import com.smartcare.service.MedicalRecordService;
import com.smartcare.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Module 16 - Digital Prescription. */
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordService medicalRecordService;

    @Override
    @Transactional
    public PrescriptionResponse create(Long doctorId, PrescriptionCreateRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + doctorId));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

        // Business Rule #7 - doctors can only act on their assigned appointments.
        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedAccessException("This appointment is not assigned to you");
        }

        Prescription prescription = Prescription.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(doctor)
                .diagnosisNotes(request.getDiagnosisNotes())
                .build();

        for (PrescriptionMedicineDto dto : request.getMedicines()) {
            prescription.addMedicine(PrescriptionMedicine.builder()
                    .medicineName(dto.getMedicineName())
                    .dosage(dto.getDosage())
                    .frequency(dto.getFrequency())
                    .duration(dto.getDuration())
                    .instructions(dto.getInstructions())
                    .build());
        }

        prescription = prescriptionRepository.save(prescription);

        medicalRecordService.addEntry(appointment, MedicalRecordType.PRESCRIPTION,
                "Prescription added by Dr. " + doctor.getFullName());
        if (request.getDiagnosisNotes() != null && !request.getDiagnosisNotes().isBlank()) {
            medicalRecordService.addEntry(appointment, MedicalRecordType.DIAGNOSIS, request.getDiagnosisNotes());
        }

        return PrescriptionMapper.toResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getById(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .map(PrescriptionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + prescriptionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getByAppointment(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId)
                .map(PrescriptionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No prescription found for appointment id: " + appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getForPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(PrescriptionMapper::toResponse)
                .toList();
    }
}
