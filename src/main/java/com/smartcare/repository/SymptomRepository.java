package com.smartcare.repository;

import com.smartcare.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {

    List<Symptom> findByActiveTrue();

    Optional<Symptom> findByNameIgnoreCase(String name);

    List<Symptom> findByNameContainingIgnoreCaseAndActiveTrue(String namePart);
}
