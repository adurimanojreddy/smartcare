package com.smartcare.dto.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Module 8 - final booking confirmation once a slot has been chosen. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentBookingRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private Long slotId;

    /** Null = booking for the logged-in patient themself. */
    private Long familyMemberId;

    @NotNull
    private String consultationType; // IN_PERSON / ONLINE

    @Builder.Default
    private boolean notifyOnEarlierSlot = false;
}
