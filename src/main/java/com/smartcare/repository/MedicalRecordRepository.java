package com.smartcare.repository;

import com.smartcare.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // Digital medical timeline (Module 15) - patient's own records, newest first.
    List<MedicalRecord> findByPatientIdAndFamilyMemberIsNullOrderByRecordDateDesc(Long patientId);

    List<MedicalRecord> findByFamilyMemberIdOrderByRecordDateDesc(Long familyMemberId);

    List<MedicalRecord> findByAppointmentId(Long appointmentId);
}
