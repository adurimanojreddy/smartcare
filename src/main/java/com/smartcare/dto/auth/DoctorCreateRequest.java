package com.smartcare.dto.auth;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Admin-only: create a new doctor account + profile (Module 1). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String temporaryPassword;

    private String phone;

    @NotBlank
    private String specialization;

    @NotNull
    private Long departmentId;

    private String qualification;

    @Min(0)
    private Integer experienceYears;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal consultationFee;

    @Min(5)
    private Integer consultationDurationMinutes;
}
