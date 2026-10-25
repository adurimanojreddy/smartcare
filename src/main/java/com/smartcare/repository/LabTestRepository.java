package com.smartcare.repository;

import com.smartcare.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    List<LabTest> findByPatientIdOrderByRequestedDateDesc(Long patientId);

    List<LabTest> findByAppointmentId(Long appointmentId);

    List<LabTest> findByRequestedById(Long doctorId);
}
