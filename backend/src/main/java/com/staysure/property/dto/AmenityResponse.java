package com.staysure.property.dto;

import java.time.LocalDateTime;

public record AmenityResponse(
        Long id,
        String name,
        String code,
        String icon,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
