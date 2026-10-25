package com.smartcare.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String specialization;
    private Long departmentId;
    private String departmentName;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private Integer consultationDurationMinutes;
    private String status;
    private String profilePhotoUrl;
}
