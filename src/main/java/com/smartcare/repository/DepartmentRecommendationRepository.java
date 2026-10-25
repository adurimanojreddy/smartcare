package com.smartcare.repository;

import com.smartcare.entity.DepartmentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartmentRecommendationRepository extends JpaRepository<DepartmentRecommendation, Long> {

    List<DepartmentRecommendation> findBySymptomIdIn(List<Long> symptomIds);

    List<DepartmentRecommendation> findByDepartmentId(Long departmentId);

    // Aggregates weight per department across all matched symptoms - the
    // core of the rule-based (non-diagnostic) recommendation in Module 6.
    @Query("""
            SELECT dr.department.id, dr.department.name, SUM(dr.weight) AS totalWeight
            FROM DepartmentRecommendation dr
            WHERE dr.symptom.id IN :symptomIds
            GROUP BY dr.department.id, dr.department.name
            ORDER BY totalWeight DESC
            """)
    List<Object[]> aggregateDepartmentScores(@Param("symptomIds") List<Long> symptomIds);
}
