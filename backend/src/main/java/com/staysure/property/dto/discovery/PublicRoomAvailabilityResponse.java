package com.staysure.property.dto.discovery;

import com.staysure.property.enums.FurnishingType;
import com.staysure.property.enums.SharingType;

import java.math.BigDecimal;

public record PublicRoomAvailabilityResponse(
        Long roomId,
        String roomNumber,
        SharingType sharingType,
        BigDecimal monthlyRent,
        BigDecimal securityDeposit,
        Integer capacity,
        long availableBeds,
        boolean acAvailable,
        boolean attachedBathroom,
        FurnishingType furnishingType
) {
}
