package com.smartcare.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Module 18 - extended analytics for charts. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private Map<String, Long> dailyAppointments;
    private Map<String, Long> weeklyAppointments;
    private Map<String, Long> monthlyAppointments;

    private List<DepartmentCount> mostBookedDepartments;
    private List<DoctorUtilization> doctorUtilization;

    private double cancellationRatePercent;
    private double noShowRatePercent;
    private double averageWaitingMinutes;

    private BigDecimal totalRevenue;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentCount {
        private String departmentName;
        private long appointmentCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorUtilization {
        private Long doctorId;
        private String doctorName;
        private long appointmentCount;
    }
}
