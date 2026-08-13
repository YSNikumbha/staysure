package com.staysure.property.dto;

import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;

import java.time.LocalDateTime;

public record PropertySummaryResponse(
        Long id,
        String name,
        String slug,
        String area,
        String city,
        String state,
        PropertyStatus status,
        PropertyVerificationStatus verificationStatus,
        String coverImageUrl,
        long roomCount,
        long bedCount,
        long availableBedCount,
        LocalDateTime createdAt
) {
}
