package com.smartcare.dto.recommendation;

import com.smartcare.dto.doctor.DoctorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 6 response. `disclaimer` is always populated and must be shown
 * to the user - this is appointment/department guidance, NOT a diagnosis
 * (see Module 29).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRecommendationResponse {

    private Long recommendedDepartmentId;
    private String recommendedDepartmentName;

    private List<DoctorResponse> availableDoctors;

    private LocalDateTime earliestAvailableSlot;

    @Builder.Default
    private String disclaimer =
            "This suggestion is for appointment/department guidance only and is not a medical diagnosis. "
          + "Please consult a doctor for an accurate assessment.";
}
