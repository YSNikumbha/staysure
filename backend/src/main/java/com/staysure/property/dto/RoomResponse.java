package com.staysure.property.dto;

import com.staysure.property.enums.FurnishingType;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.enums.SharingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(
        Long id,
        Long floorId,
        Long propertyId,
        String roomNumber,
        String roomName,
        SharingType sharingType,
        Integer capacity,
        BigDecimal monthlyRent,
        BigDecimal securityDeposit,
        boolean acAvailable,
        boolean attachedBathroom,
        FurnishingType furnishingType,
        RoomStatus status,
        String description,
        long bedCount,
        List<BedResponse> beds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
