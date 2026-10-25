package com.smartcare.service;

import com.smartcare.dto.medicalrecord.MedicalTimelineEntryResponse;
import com.smartcare.entity.Appointment;
import com.smartcare.entity.enums.MedicalRecordType;

import java.util.List;

public interface MedicalRecordService {

    /** Module 15 - adds one entry to the patient's (or family member's) digital medical timeline. */
    void addEntry(Appointment appointment, MedicalRecordType type, String description);

    /** Timeline for the account owner's own records (not family members). */
    List<MedicalTimelineEntryResponse> getTimelineForPatient(Long patientId);

    List<MedicalTimelineEntryResponse> getTimelineForFamilyMember(Long familyMemberId);
}
