package com.smartcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SmartCare - Intelligent Hospital Appointment & Queue Management System
 *
 * Entry point for the Spring Boot application.
 *
 * NOTE: This is a portfolio/academic project. It does not provide medical
 * diagnosis and does not implement real ABHA/ABDM integration. The "smart"
 * recommendation features are rule-based department/slot suggestions only.
 */
@SpringBootApplication
@EnableScheduling // used later for reminder jobs, no-show re-checks, slot recovery notifications
@EnableAsync       // used later for sending notifications without blocking request threads
public class SmartCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCareApplication.class, args);
    }

}
