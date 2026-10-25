package com.smartcare.service.impl;

import com.smartcare.dto.department.DepartmentRequest;
import com.smartcare.dto.department.DepartmentResponse;
import com.smartcare.entity.Department;
import com.smartcare.exception.DepartmentNotFoundException;
import com.smartcare.exception.DuplicateResourceException;
import com.smartcare.mapper.DepartmentMapper;
import com.smartcare.repository.DepartmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(d -> DepartmentMapper.toResponse(d, doctorRepository.findByDepartmentId(d.getId()).size()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = findOrThrow(id);
        return DepartmentMapper.toResponse(department, doctorRepository.findByDepartmentId(id).size());
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A department named '" + request.getName() + "' already exists");
        }
        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();
        department = departmentRepository.save(department);
        return DepartmentMapper.toResponse(department, 0);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findOrThrow(id);
        if (request.getName() != null) department.setName(request.getName());
        if (request.getDescription() != null) department.setDescription(request.getDescription());
        department = departmentRepository.save(department);
        return DepartmentMapper.toResponse(department, doctorRepository.findByDepartmentId(id).size());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = findOrThrow(id);
        // Soft-delete: departments with historical doctors/appointments
        // should not vanish and break foreign-key history.
        department.setActive(false);
        departmentRepository.save(department);
    }

    private Department findOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + id));
    }
}
