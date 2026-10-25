package com.smartcare.service;

import com.smartcare.entity.enums.NoShowRisk;

public interface NoShowRiskService {

    /**
     * Rule-based (not ML) no-show risk assessment for a patient about to
     * book an appointment (Module 12). Factors: prior no-shows, prior
     * cancellations, appointment history size. Thresholds are configurable
     * via application.yml rather than hardcoded.
     */
    NoShowRisk assessRisk(Long patientId);
}
