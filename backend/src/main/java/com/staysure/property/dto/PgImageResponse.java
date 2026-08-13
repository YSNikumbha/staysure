package com.staysure.property.dto;

import com.staysure.property.enums.ImageCategory;

import java.time.LocalDateTime;

public record PgImageResponse(
        Long id,
        Long propertyId,
        String imageUrl,
        ImageCategory category,
        boolean coverImage,
        Integer sortOrder,
        LocalDateTime createdAt
) {
}
