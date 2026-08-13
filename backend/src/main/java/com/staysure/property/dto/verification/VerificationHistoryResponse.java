package com.staysure.property.dto.verification;

import com.staysure.property.enums.PropertyVerificationStatus;

import java.time.LocalDateTime;

public record VerificationHistoryResponse(
        Long id,
        Long propertyId,
        PropertyVerificationStatus previousStatus,
        PropertyVerificationStatus newStatus,
        String remarks,
        Long actionBy,
        LocalDateTime createdAt
) {
}
