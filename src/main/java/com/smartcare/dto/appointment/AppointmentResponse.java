package com.smartcare.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private String appointmentCode;

    private Long patientId;
    private String patientName;

    private Long familyMemberId;
    private String familyMemberName;

    private Long doctorId;
    private String doctorName;

    private Long departmentId;
    private String departmentName;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private String status;
    private String consultationType;
    private String paymentStatus;

    private Integer tokenNumber;
    private String qrCodeImageBase64; // rendered PNG, base64-encoded (Module 10)

    private LocalDateTime checkInTime;
    private String meetingLink;
    private String noShowRisk;
    private boolean notifyOnEarlierSlot;

    private LocalDateTime createdAt;
}
