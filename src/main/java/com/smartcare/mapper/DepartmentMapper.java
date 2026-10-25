package com.smartcare.mapper;

import com.smartcare.dto.department.DepartmentResponse;
import com.smartcare.entity.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {}

    public static DepartmentResponse toResponse(Department d, long doctorCount) {
        if (d == null) return null;
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .active(d.isActive())
                .doctorCount(doctorCount)
                .build();
    }
}
