package com.staysure.booking.dto;

import com.staysure.property.enums.FurnishingType;
import com.staysure.property.enums.SharingType;

import java.math.BigDecimal;

public record BookingRoomSummary(
        Long id,
        String roomNumber,
        SharingType sharingType,
        BigDecimal monthlyRent,
        BigDecimal securityDeposit,
        Integer capacity,
        boolean acAvailable,
        boolean attachedBathroom,
        FurnishingType furnishingType
) {
}
