package com.smartcare.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Module 18 - top-line admin dashboard counters. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long todaysAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long waitingPatients;
    private long noShows;
    private long totalDoctors;
    private long totalPatients;
}
