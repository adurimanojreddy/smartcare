package com.smartcare.dto.recommendation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/** Module 7 - smart appointment slot recommendation input. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotRecommendationRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDate preferredDate;

    private LocalTime preferredStartTime;
    private LocalTime preferredEndTime;
}
