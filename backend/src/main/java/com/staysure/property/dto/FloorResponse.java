package com.staysure.property.dto;

import com.staysure.property.enums.FloorStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FloorResponse(
        Long id,
        Long propertyId,
        String name,
        Integer floorNumber,
        String description,
        FloorStatus status,
        long roomCount,
        long bedCount,
        List<RoomResponse> rooms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
