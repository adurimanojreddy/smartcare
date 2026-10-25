package com.smartcare.service;

import com.smartcare.entity.enums.AppointmentStatus;
import com.smartcare.entity.enums.NoShowRisk;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.service.impl.NoShowRiskServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Module 27 - rule-based no-show risk engine (Module 12). */
@ExtendWith(MockitoExtension.class)
class NoShowRiskServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private NoShowRiskServiceImpl noShowRiskService;

    private void setThresholds(int highNoShow, int mediumNoShow, int mediumCancel) {
        ReflectionTestUtils.setField(noShowRiskService, "highRiskNoShowCount", highNoShow);
        ReflectionTestUtils.setField(noShowRiskService, "mediumRiskNoShowCount", mediumNoShow);
        ReflectionTestUtils.setField(noShowRiskService, "mediumRiskCancelCount", mediumCancel);
    }

    @Test
    void noHistory_isLowRisk() {
        setThresholds(2, 1, 3);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.NO_SHOW)).thenReturn(0L);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.CANCELLED)).thenReturn(0L);

        assertThat(noShowRiskService.assessRisk(1L)).isEqualTo(NoShowRisk.LOW);
    }

    @Test
    void oneNoShow_isMediumRisk() {
        setThresholds(2, 1, 3);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.NO_SHOW)).thenReturn(1L);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.CANCELLED)).thenReturn(0L);

        assertThat(noShowRiskService.assessRisk(1L)).isEqualTo(NoShowRisk.MEDIUM);
    }

    @Test
    void twoNoShows_isHighRisk() {
        setThresholds(2, 1, 3);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.NO_SHOW)).thenReturn(2L);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.CANCELLED)).thenReturn(0L);

        assertThat(noShowRiskService.assessRisk(1L)).isEqualTo(NoShowRisk.HIGH);
    }

    @Test
    void manyCancellationsWithoutNoShows_isMediumRisk() {
        setThresholds(2, 1, 3);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.NO_SHOW)).thenReturn(0L);
        when(appointmentRepository.countPatientAppointmentsByStatus(1L, AppointmentStatus.CANCELLED)).thenReturn(3L);

        assertThat(noShowRiskService.assessRisk(1L)).isEqualTo(NoShowRisk.MEDIUM);
    }
}
