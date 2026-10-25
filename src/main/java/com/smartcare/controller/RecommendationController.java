package com.smartcare.controller;

import com.smartcare.dto.recommendation.DepartmentRecommendationResponse;
import com.smartcare.dto.recommendation.SlotRecommendationRequest;
import com.smartcare.dto.recommendation.SlotRecommendationResponse;
import com.smartcare.dto.recommendation.SymptomCheckRequest;
import com.smartcare.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Module 6/7/20 - Smart (rule-based, non-diagnostic) recommendations.
 * See DepartmentRecommendationResponse's built-in disclaimer (Module 29).
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/department")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<DepartmentRecommendationResponse> recommendDepartment(
            @Valid @RequestBody SymptomCheckRequest request) {
        return ResponseEntity.ok(recommendationService.recommendDepartment(request));
    }

    @PostMapping("/slots")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<SlotRecommendationResponse> recommendSlotsBody(
            @Valid @RequestBody SlotRecommendationRequest request) {
        return ResponseEntity.ok(recommendationService.recommendSlots(request));
    }

    /** Module 20's documented shape: GET /api/recommendations/slots with query params. */
    @GetMapping("/slots")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<SlotRecommendationResponse> recommendSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preferredDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime preferredStartTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime preferredEndTime) {
        SlotRecommendationRequest request = SlotRecommendationRequest.builder()
                .doctorId(doctorId)
                .preferredDate(preferredDate)
                .preferredStartTime(preferredStartTime)
                .preferredEndTime(preferredEndTime)
                .build();
        return ResponseEntity.ok(recommendationService.recommendSlots(request));
    }
}
