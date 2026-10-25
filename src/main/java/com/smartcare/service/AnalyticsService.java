package com.smartcare.service;

import com.smartcare.dto.analytics.AdminDashboardResponse;
import com.smartcare.dto.analytics.AnalyticsResponse;

import java.time.LocalDate;

public interface AnalyticsService {

    AdminDashboardResponse getDashboard();

    /** Extended analytics (daily/weekly/monthly counts, rates, revenue) for a date range. */
    AnalyticsResponse getAnalytics(LocalDate from, LocalDate to);
}
