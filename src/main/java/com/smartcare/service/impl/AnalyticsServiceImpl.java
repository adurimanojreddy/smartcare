package com.smartcare.service.impl;

import com.smartcare.dto.analytics.AdminDashboardResponse;
import com.smartcare.dto.analytics.AnalyticsResponse;
import com.smartcare.entity.enums.AppointmentStatus;
import com.smartcare.entity.enums.QueueStatus;
import com.smartcare.repository.AppointmentRepository;
import com.smartcare.repository.DoctorRepository;
import com.smartcare.repository.PatientRepository;
import com.smartcare.repository.PaymentRepository;
import com.smartcare.repository.QueueRepository;
import com.smartcare.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Module 18 - Admin Dashboard + Analytics. Aggregation queries live on the
 * relevant repositories; this service just assembles them into the two
 * response shapes the admin UI needs.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AppointmentRepository appointmentRepository;
    private final QueueRepository queueRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PaymentRepository paymentRepository;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();

        long todaysAppointments = countTodaysAppointments(today);

        long completed = countByStatusToday(today, AppointmentStatus.COMPLETED);
        long cancelled = countByStatusToday(today, AppointmentStatus.CANCELLED);
        long noShows = countByStatusToday(today, AppointmentStatus.NO_SHOW);
        long waitingPatients = queueRepository.countByQueueDateAndStatus(today, QueueStatus.WAITING);

        return AdminDashboardResponse.builder()
                .todaysAppointments(todaysAppointments)
                .completedAppointments(completed)
                .cancelledAppointments(cancelled)
                .waitingPatients(waitingPatients)
                .noShows(noShows)
                .totalDoctors(doctorRepository.count())
                .totalPatients(patientRepository.count())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(LocalDate from, LocalDate to) {
        List<Object[]> statusCounts = appointmentRepository.countByStatusBetweenDates(from, to);

        long total = 0, cancelled = 0, noShow = 0;
        for (Object[] row : statusCounts) {
            AppointmentStatus status = (AppointmentStatus) row[0];
            long count = (Long) row[1];
            total += count;
            if (status == AppointmentStatus.CANCELLED) cancelled = count;
            if (status == AppointmentStatus.NO_SHOW) noShow = count;
        }

        double cancellationRate = total == 0 ? 0.0 : (cancelled * 100.0) / total;
        double noShowRate = total == 0 ? 0.0 : (noShow * 100.0) / total;

        List<AnalyticsResponse.DepartmentCount> mostBooked = appointmentRepository
                .mostBookedDepartments(from, to).stream()
                .map(row -> AnalyticsResponse.DepartmentCount.builder()
                        .departmentName((String) row[0])
                        .appointmentCount((Long) row[1])
                        .build())
                .toList();

        List<AnalyticsResponse.DoctorUtilization> utilization = appointmentRepository
                .doctorUtilization(from, to).stream()
                .map(row -> AnalyticsResponse.DoctorUtilization.builder()
                        .doctorId((Long) row[0])
                        .doctorName((String) row[1])
                        .appointmentCount((Long) row[2])
                        .build())
                .toList();

        BigDecimal revenue = paymentRepository.sumRevenueBetween(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        Map<String, Long> daily = buildDailyBuckets(from, to);
        Map<String, Long> weekly = buildWeeklyBuckets(from, to);
        Map<String, Long> monthly = buildMonthlyBuckets(from, to);

        return AnalyticsResponse.builder()
                .dailyAppointments(daily)
                .weeklyAppointments(weekly)
                .monthlyAppointments(monthly)
                .mostBookedDepartments(mostBooked)
                .doctorUtilization(utilization)
                .cancellationRatePercent(round2(cancellationRate))
                .noShowRatePercent(round2(noShowRate))
                .averageWaitingMinutes(0.0) // populated once real queue-duration tracking is added
                .totalRevenue(revenue)
                .build();
    }

    private long countTodaysAppointments(LocalDate today) {
        List<Object[]> statusCounts = appointmentRepository.countByStatusBetweenDates(today, today);
        long total = 0;
        for (Object[] row : statusCounts) {
            total += (Long) row[1];
        }
        return total;
    }

    private long countByStatusToday(LocalDate today, AppointmentStatus status) {
        return appointmentRepository.findByStatusAndAppointmentDate(status, today).size();
    }

    private Map<String, Long> buildDailyBuckets(LocalDate from, LocalDate to) {
        Map<String, Long> buckets = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            buckets.put(d.format(DAY_FMT), (long) countTodaysAppointments(d));
        }
        return buckets;
    }

    private Map<String, Long> buildWeeklyBuckets(LocalDate from, LocalDate to) {
        Map<String, Long> buckets = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusWeeks(1)) {
            String key = d.get(IsoFields.WEEK_BASED_YEAR) + "-W" + d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            LocalDate weekEnd = d.plusDays(6).isAfter(to) ? to : d.plusDays(6);
            List<Object[]> statusCounts = appointmentRepository.countByStatusBetweenDates(d, weekEnd);
            long total = statusCounts.stream().mapToLong(r -> (Long) r[1]).sum();
            buckets.put(key, total);
        }
        return buckets;
    }

    private Map<String, Long> buildMonthlyBuckets(LocalDate from, LocalDate to) {
        Map<String, Long> buckets = new LinkedHashMap<>();
        LocalDate cursor = from.withDayOfMonth(1);
        while (!cursor.isAfter(to)) {
            LocalDate monthEnd = cursor.withDayOfMonth(cursor.lengthOfMonth());
            LocalDate rangeStart = cursor.isBefore(from) ? from : cursor;
            LocalDate rangeEnd = monthEnd.isAfter(to) ? to : monthEnd;
            List<Object[]> statusCounts = appointmentRepository.countByStatusBetweenDates(rangeStart, rangeEnd);
            long total = statusCounts.stream().mapToLong(r -> (Long) r[1]).sum();
            buckets.put(cursor.getYear() + "-" + String.format("%02d", cursor.getMonthValue()), total);
            cursor = cursor.plusMonths(1);
        }
        return buckets;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
