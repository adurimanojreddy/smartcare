package com.smartcare.dto.recommendation;

import com.smartcare.dto.slot.SlotResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotRecommendationResponse {

    /** The single best-match slot, marked with a star in the UI (Module 7). */
    private SlotResponse recommendedSlot;

    private List<SlotResponse> alternativeSlots;
}
