package com.smartcare.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates human-readable appointment codes like HSP-2026-000123 (Module 10).
 * The counter is process-local; the DB unique constraint on appointment_code
 * is the real guarantee against collisions in a multi-instance deployment.
 */
@Component
public class AppointmentCodeGenerator {

    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 1_000_000);

    public String generate() {
        int year = Year.now().getValue();
        long seq = counter.incrementAndGet() % 1_000_000;
        return String.format("HSP-%d-%06d", year, seq);
    }
}
