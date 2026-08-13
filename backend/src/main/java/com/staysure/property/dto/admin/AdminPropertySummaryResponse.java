package com.staysure.property.dto.admin;

import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;

import java.time.LocalDateTime;

public record AdminPropertySummaryResponse(
        Long id,
        String name,
        String ownerName,
        Long ownerId,
        String city,
        LocalDateTime submittedForVerificationAt,
        PropertyVerificationStatus verificationStatus,
        PropertyStatus status,
        long roomCount,
        long bedCount
) {
}
