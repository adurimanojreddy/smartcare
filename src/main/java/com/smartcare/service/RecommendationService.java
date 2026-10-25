package com.smartcare.service;

import com.smartcare.dto.recommendation.DepartmentRecommendationResponse;
import com.smartcare.dto.recommendation.SlotRecommendationRequest;
import com.smartcare.dto.recommendation.SlotRecommendationResponse;
import com.smartcare.dto.recommendation.SymptomCheckRequest;

public interface RecommendationService {

    /**
     * Module 6 - rule-based (NOT diagnostic) department + doctor + earliest
     * slot suggestion from a list of patient-entered symptoms.
     */
    DepartmentRecommendationResponse recommendDepartment(SymptomCheckRequest request);

    /**
     * Module 7 - given a doctor and a patient's preferred date/time window,
     * recommend the best-fit available slot plus a few alternatives.
     */
    SlotRecommendationResponse recommendSlots(SlotRecommendationRequest request);
}
