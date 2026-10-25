package com.smartcare.repository;

import com.smartcare.entity.Doctor;
import com.smartcare.entity.enums.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findByDepartmentId(Long departmentId);

    List<Doctor> findByStatus(DoctorStatus status);

    List<Doctor> findByDepartmentIdAndStatus(Long departmentId, DoctorStatus status);

    // Search by name / specialization (Module 5 - patient doctor search)
    @Query("""
            SELECT d FROM Doctor d
            WHERE (:name IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')))
              AND (:departmentId IS NULL OR d.department.id = :departmentId)
              AND d.status = com.smartcare.entity.enums.DoctorStatus.ACTIVE
            """)
    List<Doctor> searchDoctors(@Param("name") String name,
                                @Param("specialization") String specialization,
                                @Param("departmentId") Long departmentId);
}
