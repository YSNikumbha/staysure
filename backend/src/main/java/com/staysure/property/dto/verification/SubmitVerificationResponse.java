package com.staysure.property.dto.verification;

import com.staysure.property.enums.PropertyVerificationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SubmitVerificationResponse(
        Long propertyId,
        PropertyVerificationStatus verificationStatus,
        LocalDateTime submittedForVerificationAt,
        List<String> missingItems
) {
}
