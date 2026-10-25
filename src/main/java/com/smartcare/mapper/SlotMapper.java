package com.smartcare.mapper;

import com.smartcare.dto.slot.SlotResponse;
import com.smartcare.entity.AppointmentSlot;

public final class SlotMapper {
    private SlotMapper() {}

    public static SlotResponse toResponse(AppointmentSlot s) {
        if (s == null) return null;
        return SlotResponse.builder()
                .id(s.getId())
                .doctorId(s.getDoctor().getId())
                .slotDate(s.getSlotDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .status(s.getStatus().name())
                .build();
    }
}
