package com.smartcare.service.impl;

import com.smartcare.entity.enums.AppointmentStatus;
import com.smartcare.entity.enums.NoShowRisk;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.service.NoShowRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple rule-based no-show risk engine (Module 12). This is explicitly
 * NOT a machine-learning model - see Module 12/29's instruction not to
 * overstate the AI involved.
 */
@Service
@RequiredArgsConstructor
public class NoShowRiskServiceImpl implements NoShowRiskService {

    private final AppointmentRepository appointmentRepository;

    @Value("${smartcare.noshow.high-risk-noshow-count:2}")
    private int highRiskNoShowCount;

    @Value("${smartcare.noshow.medium-risk-noshow-count:1}")
    private int mediumRiskNoShowCount;

    @Value("${smartcare.noshow.medium-risk-cancel-count:3}")
    private int mediumRiskCancelCount;

    @Override
    @Transactional(readOnly = true)
    public NoShowRisk assessRisk(Long patientId) {
        long noShowCount = appointmentRepository.countPatientAppointmentsByStatus(patientId, AppointmentStatus.NO_SHOW);
        long cancelledCount = appointmentRepository.countPatientAppointmentsByStatus(patientId, AppointmentStatus.CANCELLED);

        if (noShowCount >= highRiskNoShowCount) {
            return NoShowRisk.HIGH;
        }
        if (noShowCount >= mediumRiskNoShowCount || cancelledCount >= mediumRiskCancelCount) {
            return NoShowRisk.MEDIUM;
        }
        return NoShowRisk.LOW;
    }
}
