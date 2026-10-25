package com.smartcare.service.impl;

import com.smartcare.dto.medicalrecord.MedicalTimelineEntryResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.MedicalRecord;
import com.smartcare.entity.enums.MedicalRecordType;
import com.smartcare.mapper.MedicalRecordMapper;
import com.smartcare.repository.MedicalRecordRepository;
import com.smartcare.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    @Transactional
    public void addEntry(Appointment appointment, MedicalRecordType type, String description) {
        MedicalRecord record = MedicalRecord.builder()
                .patient(appointment.getPatient())
                .familyMember(appointment.getFamilyMember())
                .appointment(appointment)
                .recordType(type)
                .description(description)
                .recordDate(LocalDate.now())
                .build();
        medicalRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalTimelineEntryResponse> getTimelineForPatient(Long patientId) {
        return medicalRecordRepository.findByPatientIdAndFamilyMemberIsNullOrderByRecordDateDesc(patientId).stream()
                .map(MedicalRecordMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalTimelineEntryResponse> getTimelineForFamilyMember(Long familyMemberId) {
        return medicalRecordRepository.findByFamilyMemberIdOrderByRecordDateDesc(familyMemberId).stream()
                .map(MedicalRecordMapper::toResponse)
                .toList();
    }
}
