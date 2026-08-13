package com.staysure.property.dto;

import com.staysure.property.enums.BedStatus;

import java.time.LocalDateTime;

public record BedResponse(
        Long id,
        Long roomId,
        Long propertyId,
        String bedNumber,
        String bedLabel,
        BedStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
