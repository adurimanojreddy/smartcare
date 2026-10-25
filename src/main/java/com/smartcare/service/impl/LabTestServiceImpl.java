package com.smartcare.service.impl;

import com.smartcare.dto.labtest.LabTestRequest;
import com.smartcare.dto.labtest.LabTestResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.Doctor;
import com.smartcare.entity.LabTest;
import com.smartcare.entity.enums.LabTestStatus;
import com.smartcare.entity.enums.MedicalRecordType;
import com.smartcare.exception.AppointmentNotFoundException;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.ResourceNotFoundException;
import com.smartcare.exception.UnauthorizedAccessException;
import com.smartcare.mapper.LabTestMapper;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.LabTestRepository;
import com.smartcare.service.LabTestService;
import com.smartcare.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabTestServiceImpl implements LabTestService {

    private final LabTestRepository labTestRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordService medicalRecordService;

    @Override
    @Transactional
    public LabTestResponse request(Long doctorId, LabTestRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + doctorId));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedAccessException("This appointment is not assigned to you");
        }

        LabTest labTest = LabTest.builder()
                .patient(appointment.getPatient())
                .appointment(appointment)
                .requestedBy(doctor)
                .testName(request.getTestName())
                .status(LabTestStatus.REQUESTED)
                .build();
        labTest = labTestRepository.save(labTest);

        medicalRecordService.addEntry(appointment, MedicalRecordType.LAB_REQUEST,
                request.getTestName() + " requested by Dr. " + doctor.getFullName());

        return LabTestMapper.toResponse(labTest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponse> getForPatient(Long patientId) {
        return labTestRepository.findByPatientIdOrderByRequestedDateDesc(patientId).stream()
                .map(LabTestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public LabTestResponse updateResult(Long labTestId, String resultNotes, String status) {
        LabTest labTest = labTestRepository.findById(labTestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + labTestId));

        if (resultNotes != null) labTest.setResultNotes(resultNotes);
        if (status != null) labTest.setStatus(LabTestStatus.valueOf(status.toUpperCase()));

        labTest = labTestRepository.save(labTest);

        if (labTest.getStatus() == LabTestStatus.COMPLETED && labTest.getAppointment() != null) {
            medicalRecordService.addEntry(labTest.getAppointment(), MedicalRecordType.LAB_REPORT,
                    labTest.getTestName() + " results available");
        }

        return LabTestMapper.toResponse(labTest);
    }
}
