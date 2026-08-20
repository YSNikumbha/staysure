package com.staysure.booking.dto;

import com.staysure.property.enums.BedStatus;

public record BookingBedSummary(
        Long id,
        String bedNumber,
        String bedLabel,
        BedStatus status
) {
}
