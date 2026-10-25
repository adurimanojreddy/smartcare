package com.smartcare.dto.doctor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Used by both Doctor (own profile) and Admin (any doctor) update flows. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorUpdateRequest {

    @Size(max = 150)
    private String fullName;

    private String specialization;

    private Long departmentId;

    private String qualification;

    @Min(0)
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal consultationFee;

    @Min(5)
    private Integer consultationDurationMinutes;

    private String status; // ACTIVE / ON_LEAVE / INACTIVE - admin only in practice

    private String profilePhotoUrl;
}
