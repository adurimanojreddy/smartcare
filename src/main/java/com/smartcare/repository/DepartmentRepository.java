package com.smartcare.repository;

import com.smartcare.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByNameIgnoreCase(String name);

    List<Department> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}
