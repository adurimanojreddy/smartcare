package com.smartcare.service;

import com.smartcare.dto.department.DepartmentRequest;
import com.smartcare.dto.department.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> getAll();

    DepartmentResponse getById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);
}
